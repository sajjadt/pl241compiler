#!/bin/bash
ls -1 Vis/*.dot | while read g; do
	dot "${g}" -Tpng > "${g}.png"
done
