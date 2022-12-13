[
  {
    "name": "${task_name}",
    "image": "${aws_ecr_url}:${tag}",
    "environment": [
      {
        "name": "SERVER_PORT",
        "value": "${server_port}"
      },
      {
        "name": "CLIENT_ID",
        "value": "${client_id}"
      },
      {
        "name": "CLIENT_SECRET",
        "value": "${client_secret}"
      },
      {
        "name": "REDIRECT_URI",
        "value": "${redirect_uri}"
      },
      {
        "name": "TOKEN_URI",
        "value": "${token_uri}"
      },
      {
        "name": "PROTECTED_URI",
        "value": "${protected_uri}"
      },
      {
        "name": "USER_URI",
        "value": "${user_uri}"
      },
      {
        "name": "AUTHORISE_URI",
        "value": "${authorise_uri}"
      },
      {
        "name": "API_URI",
        "value": "${api_uri}"
      }
    ],
    "portMappings": [
      {
        "protocol": "tcp",
        "containerPort": ${server_port
        },
        "hostPort": ${server_port
        }
      }
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "${cloudwatch_log_group_name}",
        "awslogs-region": "${region}",
        "awslogs-stream-prefix": "${cloudwatch_log_prefix}"
      }
    }
  }
]