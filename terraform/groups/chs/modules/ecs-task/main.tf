data "template_file" "container_definitions" {
  template = file("${path.module}/templates/container_definitions.json.tpl")
  vars = {
    task_name                 = var.service_name
    aws_ecr_url               = var.ecr_url
    tag                       = var.container_image_version
    server_port               = var.container_port
    client_id                 = var.client_id
    client_secret             = var.client_secret
    redirect_uri              = var.redirect_uri
    token_uri                 = var.token_uri
    protected_uri             = var.protected_uri
    user_uri                  = var.user_uri
    authorise_uri             = var.authorise_uri
    cloudwatch_log_group_name = var.log_group_name
    cloudwatch_log_prefix     = var.log_prefix
    region                    = var.region
  }
}

resource "aws_ecs_task_definition" "main" {
  family                   = var.service_name
  network_mode             = "awsvpc"
  execution_role_arn       = var.ecs_task_role_arn
  cpu                      = var.task_cpu
  memory                   = var.task_memory
  requires_compatibilities = ["FARGATE"]
  container_definitions    = data.template_file.container_definitions.rendered

  tags = var.tags
}

resource "aws_ecs_service" "main" {
  name            = var.service_name
  cluster         = var.ecs_cluster_id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.instance.id]
    subnets          = var.subnet_ids
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = var.target_group_arn
    container_name   = var.service_name
    container_port   = var.container_port
  }

  tags = var.tags
}


resource "aws_security_group" "instance" {
  description = "Restricts access to ${var.service_name} instance"
  name        = "${var.service_name}-instance"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [var.lb_security_group_id]
  }

  egress {
    description = "Allow outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.service_name}-instance"
  })
}
