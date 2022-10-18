variable "region" {
  type        = string
  description = "AWS region for deployment"
}

variable "environment" {
  type        = string
  description = "The environment name to be used when creating AWS resources"
}

variable "vpc_name" {
  type        = string
  description = "The name of the VPC to be used for AWS resources"
}

variable "service_name" {
  type        = string
  description = "The service name to be used when creating AWS resources"
  default     = "test-harness"
}

variable "ecr_url" {
  type = string
}

variable "container_image_version" {
  type        = string
  description = "Version of the docker image to deploy"
  default     = "latest"
}

variable "task_cpu" {
  type        = number
  description = "The cpu unit limit for the ECS task"
}

variable "task_memory" {
  type        = number
  description = "The memory limit for the ECS task"
}

variable "route53_zone" {
  type        = string
  description = "The Route53 hosted zone to use for DNS records"
  default     = "N/A"
}

variable "create_route53_record" {
  type        = bool
  description = "Should a Route53 record be created"
}

variable "domain_name" {
  type        = string
  description = "The domain name to use for the application"
}

variable "certificate_domain" {
  type        = string
  description = "The domain used to look up existing certificates"
  default     = "N/A"
}

variable "applications" {
  type    = list(string)
  default = ["webapps", "api-filing", "chs-auth"]
}

variable "clients" {
  type = list(object({
    client_id     = string
    client_secret = string
  }))
  default = [
    {
      client_id     = "ApiFilingWebClient"
      client_secret = "ApiFilingWebClient"
    },
    {
      client_id     = "ApiFilingM2MClient"
      client_secret = "ApiFilingM2MClient"
    },
    {
      client_id     = "CHSWebClient"
      client_secret = "CHSWebClient"
    }
  ]
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