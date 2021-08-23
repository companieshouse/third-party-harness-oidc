variable "service_name" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "target_port" {
  type = number
}

variable "tags" {
  type = object({
    Environment    = string
    Service        = string
    ServiceSubType = string
    Team           = string
  })
}