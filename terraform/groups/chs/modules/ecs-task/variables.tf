variable "service_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "ecs_cluster_id" {
  type = string
}

variable "ecs_task_role_arn" {
  type = string
}

variable "lb_security_group_id" {
  type = string
}

variable "container_image_version" {
  type = string
}

variable "ecr_url" {
  type = string
}

variable "task_cpu" {
  type = number
}

variable "task_memory" {
  type = number
}

variable "target_group_arn" {
  type = string
}

variable "container_port" {
  type = string
}

variable "client_id" {
  type = string
}

variable "client_secret" {
  type = string
}

variable "redirect_uri" {
  type = string
}

variable "token_uri" {
  type = string
}

variable "protected_uri" {
  type = string
}

variable "user_uri" {
  type = string
}

variable "authorise_uri" {
  type = string
}

variable "region" {
  type = string
}

variable "log_group_name" {
  type = string
}

variable "log_prefix" {
  type = string
}

variable "tags" {
  type = object({
    Environment    = string
    Service        = string
    ServiceSubType = string
    Team           = string
  })
}