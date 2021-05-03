package de.tadris.fitness.map;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import androidx.core.graphics.ColorUtils;


public class GradientColoringStrategy implements ColoringStrategy {

    // this color is based on https://colorbrewer2.org/#type=sequential&scheme=BuPu&n=9
    public final static String PATTERN_PURPLE   = "#4d004b #810f7c #88419d #8c6bb1 #8c96c6 #9ebcda #bfd3e6 #e0ecf4 #f7fcfd";
    public final static String PATTERN_PINK   = "#67001f #980043 #ce1256 #e7298a #df65b0 #c994c7 #d4b9da #e7e1ef #f7f4f9";

    // other "nice" patterns
    public final static String PATTERN_MAP = "#ed5f53 #ede553 #53ede5 #1a1ee0 #e01a2e";
    public final static String PATTERN_BRIGHT = "#f0f2cd #c9f28c #a4f4ef #cfd6f7 #e4cbf4 #f4c1e7 #ddb8c2";
    public final static String PATTERN_YELLOW_RED_BLUE = "#f2f215 #f2f215 #800000 #000080";/* best used with no blending */

    double value_min;
    double value_max;
    private final int ITEMS_PER_GRADIENT = 10;

    int[] colorPalette;

    /**
     * Helper function to construct a coloring strategy based on a string representation of the color
     * @param pattern a space delimited list of rgb values in the format of #rrggbb or #aarrbbgg
     * @param value_min The value that corresponds the the position of the scale
     * @param value_max The value that corresponds the the highest end of the scale
     * @param doBlend Wheter to blend from one value to the other or not
     * @return a new instance of ColoringStrategy
     */
    public static ColoringStrategy fromPattern(String pattern, double value_min, double value_max, boolean doBlend){
        String[] colorStrings = pattern.split(" ");
        assert colorStrings.length > 1; // We expect at least two colors because we "blend" between colors ..
        int[] colors = new int[colorStrings.length];
        for(int i =0 ; i < colorStrings.length ; i++){
            colors[i] = Color.parseColor(colorStrings[i]);
        }
        return new GradientColoringStrategy(colors,value_min,value_max,doBlend);
    }

    /**
     * Construct a new Gradient given the array of argb colors and a minimal max value to map
     * the color to.
     * @param colors The colors to map.
     * @param value_min The value that corresponds the the position of the scale
     * @param value_max The value that corresponds the the highest end of the scale
     * @param doBlend Wheter to blend from one value to the other or not. If blend is set to false
     *                the first color in the list will be discarded hence an additional bogus color
     *                is needed.
     */
    public GradientColoringStrategy(int[] colors, double value_min, double value_max, boolean doBlend){
        this.value_min = value_min;
        this.value_max = value_max;
        assert colors.length > 1;

        int blend_count = colors.length -1;// color -1 blends (from color n to color n+1
        int item_count  = blend_count * ITEMS_PER_GRADIENT;//We are going to create 10 items per color
        // the strategy there is to precalculate the gradient so allow cheap lookup later
        colorPalette = new int[item_count];
        for( int i =0 ; i < blend_count ; i++){
            for(int f = 0; f < ITEMS_PER_GRADIENT; f++) {
                int index = i * ITEMS_PER_GRADIENT + f;
                float ratio = (doBlend)?(float) f / ITEMS_PER_GRADIENT :1;
                colorPalette[index] = ColorUtils.blendARGB(colors[i],colors[i+1],ratio);
            }
        }
        assert value_min <= value_max;
    }

    /**
     * The GradientColoringStrategy has similarities to the Android gradient. It might be possible
     * to use the gradient here to display the gradient in the settings pane or similar
     * @return an Android gradient that is analog to this gradient
     */
    public GradientDrawable asGradientDrawable(){
        // We are returning the full colorPallete (as opposed to the initial colors given to the
        // class. The result .. should be very similar
        return new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colorPalette);
    }

    public int getColor(double value){
        //find out in what bin to map the value
        double offset =  value - value_max;
        double max_offset = value_min - value_max;
        double index = ((offset / max_offset) ) * colorPalette.length;

        // index can become negative or higher compared to the amount of control points
        // in such situation we take the edge value
        index = Math.min(index , colorPalette.length-1);//if the scale is smaller than the actual speed -> use the largest
        index = Math.max(index , 0);//same for smaller scale
        return colorPalette[(int)index];
    }
}
