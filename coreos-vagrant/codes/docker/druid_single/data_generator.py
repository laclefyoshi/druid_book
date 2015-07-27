#!/usr/bin/env python
# -*- coding: utf-8 -*-
import time
import random
import uuid
import json
import sys

output_file_name = "/opt/data/dummy_data.json"
num_lines = 100

#output_file = open(output_file_name, 'w')
output_file = sys.stdout
domains = ["az", "bz", "cz", "dz", "eu", "fr", "gy", "hu", "it", "jp",
           "kz", "ly", "mz", "nz", "om", "py", "qa", "rw", "sz", "tz",
           "uz", "vu", "ws", "yu", "zw"]
for i in range(num_lines):
    millisec = int((time.time() + i * 30) * 1000)
    domain = random.choice(domains)
    value = random.randint(0, 100)
    id = str(uuid.uuid4())
    output_file.write(json.dumps({"timestamp": millisec, "domain": domain,
                                  "value": value, "id": id}))
    output_file.write("\n")
output_file.close()

