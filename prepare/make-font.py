#!/usr/bin/env python3

import fontforge

hz = ""
for line in open("han.txt"):
    hz+=line.strip()
us = list(map(ord,hz))
f="han.ttf"
font = fontforge.open(f)
for g in font.glyphs():
    u = g.unicode
    if u >= 0x40000 or u not in us:
        font.removeGlyph(g)
font.generate("../app/src/main/res/font/han.ttf")
font.close()

