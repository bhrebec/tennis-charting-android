SVG=center_strokes.svg left_strokes.svg right_strokes.svg

XXHDPI=$(join $(addprefix ../drawable-xxhdpi/, $(dir $(SVG))), $(notdir $(SVG:.svg=.png)))
XHDPI=$(join $(addprefix ../drawable-xhdpi/, $(dir $(SVG))), $(notdir $(SVG:.svg=.png)))
HDPI=$(join $(addprefix ../drawable-hdpi/, $(dir $(SVG))), $(notdir $(SVG:.svg=.png)))
MDPI=$(join $(addprefix ../drawable-mdpi/, $(dir $(SVG))), $(notdir $(SVG:.svg=.png)))

ALL=$(MDPI) $(HDPI) $(XHDPI)

DENSITY_BASE=36
$(XXHDPI): DENSITY := $(shell echo $(DENSITY_BASE)*3 | bc)
$(XHDPI): DENSITY := $(shell echo $(DENSITY_BASE)*2 | bc)
$(HDPI): DENSITY := $(shell echo $(DENSITY_BASE)*3/2 | bc)
$(MDPI): DENSITY := $(DENSITY_BASE)

all: $(ALL)

.PHONY: clean

clean:
	rm $(ALL)

.SECONDEXPANSION:
%.png: $$(notdir $$*).svg
	inkscape -d $(DENSITY) -D -e $@ $< > /dev/null

