resource "aws_lb_target_group" "main" {
  name        = var.service_name
  port        = var.target_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    healthy_threshold   = 2
    unhealthy_threshold = 5
    timeout             = 30
    path                = "/login"
    interval            = 60
  }

  tags = var.tags
}