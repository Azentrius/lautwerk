# SPDX-License-Identifier: Apache-2.0
"""Reproduce a screenshot contrast measurement: image fg_x fg_y bg_x bg_y.

Requires Pillow. Coordinates refer to the original PNG, before any report crop.
Use solid interior text strokes and the button surface, not antialiased edges.
"""
import argparse
import json
from PIL import Image

def luminance(rgb):
    values=[c/255 for c in rgb]
    linear=[c/12.92 if c<=0.04045 else ((c+0.055)/1.055)**2.4 for c in values]
    return sum(c*w for c,w in zip(linear,[0.2126,0.7152,0.0722]))

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('image')
for name in ['fg_x','fg_y','bg_x','bg_y']:parser.add_argument(name,type=int)
args=parser.parse_args()
im=Image.open(args.image).convert('RGB')
fg=im.getpixel((args.fg_x,args.fg_y))
bg=im.getpixel((args.bg_x,args.bg_y))
light,dark=sorted([luminance(fg),luminance(bg)],reverse=True)
ratio=(light+0.05)/(dark+0.05)
print(json.dumps({'foreground':fg,'background':bg,'contrast':ratio,'passes_4_5':ratio>=4.5}))
