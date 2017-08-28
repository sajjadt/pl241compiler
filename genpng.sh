#!/bin/bash
ls -1 cfgs/*.dot | while read g; do
	dot "${g}" -Tpng > "${g}.png"
done
