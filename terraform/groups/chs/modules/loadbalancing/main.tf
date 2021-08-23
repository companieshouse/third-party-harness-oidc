data "aws_acm_certificate" "certificate" {
  domain = var.certificate_domain
}

data "aws_route53_zone" "domain" {
  count = var.create_route53_record ? 1 : 0
  name  = var.route53_zone
}

resource "aws_route53_record" "lb" {
  count   = var.create_route53_record ? length(var.applications) : 0
  name    = "${var.service_name}-${var.applications[count.index]}${var.domain_name}"
  zone_id = data.aws_route53_zone.domain.0.id
  type    = "A"
  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = false
  }
}

resource "aws_lb" "main" {
  name                             = var.service_name
  load_balancer_type               = "application"
  enable_cross_zone_load_balancing = "true"
  internal                         = var.internal
  subnets                          = var.subnet_ids
  security_groups                  = [aws_security_group.lb.id]

  tags = var.tags
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = 443
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.main.arn
  port              = 443
  protocol          = "HTTPS"
  certificate_arn   = data.aws_acm_certificate.certificate.arn

  default_action {
    target_group_arn = var.target_group_arns[0]
    type             = "forward"
  }
}

resource "aws_lb_listener_rule" "host_based_routing" {
  count        = length(var.applications) - 1
  listener_arn = aws_lb_listener.https.arn

  action {
    type             = "forward"
    target_group_arn = var.target_group_arns[count.index + 1]
  }

  condition {
    host_header {
      values = ["${var.applications[count.index + 1]}${var.domain_name}"]
    }
  }
}

resource "aws_security_group" "lb" {
  description = "Restricts access to the load balancer"
  name        = "${var.service_name}-lb"
  vpc_id      = var.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = var.ingress_cidr_blocks
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = var.ingress_cidr_blocks
  }

  egress {
    description = "Allow outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(var.tags, {
    Name = "${var.service_name}-lb"
  })
}
