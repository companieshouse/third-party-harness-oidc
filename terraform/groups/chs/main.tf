###
# Data lookups
###
data "aws_vpc" "vpc" {
  tags = {
    Name = var.vpc_name
  }
}

data "aws_subnet_ids" "application_subnets" {
  vpc_id = data.aws_vpc.vpc.id

  filter {
    name   = "tag:Name"
    values = ["*-applications-*"]
  }
}

###
# Modules
###
module "ecs" {
  source       = "./modules/ecs"
  service_name = var.service_name
  vpc_id       = data.aws_vpc.vpc.id
  tags         = local.common_tags
}

module "internal_lb" {
  source                = "./modules/loadbalancing"
  service_name          = var.service_name
  internal              = true
  vpc_id                = data.aws_vpc.vpc.id
  ingress_cidr_blocks   = ["0.0.0.0/0"]
  subnet_ids            = data.aws_subnet_ids.application_subnets.ids
  target_port           = 8090
  domain_name           = var.domain_name
  create_route53_record = var.create_route53_record
  route53_zone          = var.route53_zone
  create_certificate    = var.create_certificate
  certificate_domain    = var.certificate_domain
  tags                  = local.common_tags
}

module "test_harness" {
  source                  = "./modules/ecs-task"
  service_name            = var.service_name
  vpc_id                  = data.aws_vpc.vpc.id
  subnet_ids              = data.aws_subnet_ids.application_subnets.ids
  ecs_cluster_id          = module.ecs.cluster_id
  ecs_task_role_arn       = module.ecs.task_role_arn
  lb_security_group_id    = module.internal_lb.security_group_id
  container_image_version = var.container_image_version
  ecr_url                 = var.ecr_url
  task_cpu                = var.task_cpu
  task_memory             = var.task_memory
  target_group_arn        = module.internal_lb.target_group_arn
  container_port          = 8090
  client_id               = var.client_id
  client_secret           = var.client_secret
  redirect_uri            = var.redirect_uri
  token_uri               = var.token_uri
  protected_uri           = var.protected_uri
  user_uri                = var.user_uri
  authorise_uri           = var.authorise_uri
  region                  = var.region
  log_group_name          = "forgerock-monitoring"
  log_prefix              = "test-harness"
  tags                    = local.common_tags
}
