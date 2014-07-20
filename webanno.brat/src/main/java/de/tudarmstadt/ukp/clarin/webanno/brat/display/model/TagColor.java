/*******************************************************************************
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.tudarmstadt.ukp.clarin.webanno.brat.display.model;

import java.awt.Color;
import java.util.Collection;
import java.util.Random;

/**
 * Generate a random color for each tag while visualizing span annotation
 *
 * @author Tobias Krönke
 */
public class TagColor
{
    public final static String[] PALETTE_PASTEL = { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072",
            "#80b1d3", "#fdb462", "#b3de69", "#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f" };

    public final static String[] PALETTE_NORMAL = { "#a6cee3", "#1f78b4", "#b2df8a", "#33a02c",
            "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#cab2d6", "#6a3d9a", "#ffff99", "#b15928" };
    
    public static boolean tooSimilar(Color c, Color b)
    {
        double distance = (c.getRed() - b.getRed()) * (c.getRed() - b.getRed())
                + (c.getGreen() - b.getGreen()) * (c.getGreen() - b.getGreen())
                + (c.getBlue() - b.getBlue()) * (c.getBlue() - b.getBlue());
        if (distance < 768) {
            return true;
        }
        else {
            return false;
        }
    }

    public  static synchronized Color generateDifferingPastelColor(Collection<Color> taken)
    {
        Color seedColor = new Color(255, 255, 255);
        Random rand = new Random(0);
        
        int i = 0;
        while (true) {
            i++;
            Color c = generatePastelColor(seedColor, rand);
            boolean canAdd = true;
            for (Color d : taken) {
                if (tooSimilar(c, d)) {
                    canAdd = false;
                    break;
                }
            }
            if (canAdd) {
                return c;
            }
            else if(i==1000){// no home of getting new color not similar to this
                return c;
            }
        }
    }

    public static Color generatePastelColor(Color mix, Random random)
    {
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }

        Color color = new Color(red, green, blue);
        return color;
    }

    public static synchronized String encodeRGB(Color color)
    {
        if (null == color) {
            throw new IllegalArgumentException("NULL_COLOR_PARAMETER_ERROR_2");
        }
        return "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
    }
}
