#!/bin/bash

cd "$(dirname $0)/blocks"

BLOCKS=(
  terminal
  computer
  drive
  rs
  rs_analog
  radio
  retinal_scanner
)

SIDES='"top","bottom","front","back","side"'

COMMON='common.xcf'

function gimp_cmd() {
  (
    cat; echo 'pdb.gimp_quit(0)'
  ) | gimp --batch-interpreter=python-fu-eval -idfnb - 2>/dev/null #| grep '^ '
}

for block in ${BLOCKS[@]}; do
  echo " >>> Processing $block"
  gimp_cmd <<EOF
def x(s, b, side):
  if b != False:
    return s == side + " combined"
  else:
    return (s == "vignette") | (s == "common") | (s == "noise") | (s == side)
  
cf = "${COMMON}"
sf = "${block}.xcf"
common = pdb.gimp_xcf_load(0, cf, cf)
spec = pdb.gimp_xcf_load(0, sf, sf)

for side in ${SIDES}:
  print("     >>> Processing " + side)
  outfile = "${block}_" + side + ".png"
  
  common_o = False
  spec_o = False
  
  for layer in common.layers:
    if layer.name == side + " combined":
      common_o = layer
  
  for layer in spec.layers:
    if layer.name == side + " combined":
      spec_o = layer
    elif layer.name == side:
      spec_o = True
  
  if spec_o == False:
    for layer in common.layers:
      layer.visible = x(layer.name, common_o, side)
    tmpimg = pdb.gimp_image_duplicate(common)
    explayer = pdb.gimp_image_merge_visible_layers(tmpimg, CLIP_TO_IMAGE)
    pdb.gimp_file_save(tmpimg, explayer, outfile, outfile)
    pdb.gimp_image_delete(tmpimg)
  else:
    lsp = spec_o
    if lsp == True:
      lsp = False
    for layer in spec.layers:
      layer.visible = x(layer.name, lsp, side)
    tmpimg = pdb.gimp_image_duplicate(spec)
    explayer = pdb.gimp_image_merge_visible_layers(tmpimg, CLIP_TO_IMAGE)
    pdb.gimp_file_save(tmpimg, explayer, outfile, outfile)
    pdb.gimp_image_delete(tmpimg)
EOF
done
