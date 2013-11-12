/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.FieldMapService;
import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.pojos.svg.Element;
import com.efficio.pojos.svg.Rectangle;
import com.efficio.pojos.svg.Text;

@Service
public class FieldMapServiceImpl implements FieldMapService{
    
    private static final String EMPTY_CELL = ""; 
    private static final int RANGE_MARGIN_X = 10;
    private static final int RANGE_MARGIN_Y = 10;
    private static final int BLOCK_MARGIN_X = 15;
    private static final int BLOCK_MARGIN_Y = 15;
    private static final int CELL_WIDTH = 70;
    private static final int CELL_HEIGHT = 40;
    private static final int PLOT_MARGIN_Y = 20;
    private static final String NEXT_LINE = "\n";
    private static final String CELL_ID_PREFIX = "cell";

    
    public List<Element> createBlankFieldmap(UserFieldmap info, int startX, int startY) {
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        int rowsPerPlot = info.getNumberOfRowsPerPlot();
        boolean isSerpentine = info.getPlantingOrder() == 1;
        
        //List<String> data = createFieldmap(info);
        return createFieldmapElements(null, rows, ranges, rowsPerPlot, isSerpentine, startX, startY);
    }
    
    @Override
    public List<String> createFieldmap(UserFieldmap info) {
        List<String> names = new ArrayList<String>();
        names.add(info.getSelectedName());
        int reps = Integer.valueOf(info.getNumberOfReps());
        int entries = Integer.valueOf(info.getNumberOfEntries());
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        boolean isSerpentine = info.getPlantingOrder() == 1;

        return generateFieldmapLabels(names, reps, entries, rows, ranges, isSerpentine);
    }
    
    @Override
    public List<Element> createFieldmap(UserFieldmap info, int startX, int startY) {
        int rows = info.getNumberOfRowsInBlock();
        int ranges = info.getNumberOfRangesInBlock();
        int rowsPerPlot = info.getNumberOfRowsPerPlot();
        boolean isSerpentine = info.getPlantingOrder() == 1;
        
        List<String> data = createFieldmap(info);
        return createFieldmapElements(data, rows, ranges, rowsPerPlot, isSerpentine, startX, startY);
    }
    
    private List<Element> createFieldmapElements(List<String> data, int rows, int cols, int rowsPerPlot, boolean isSerpentine, int startX, int startY) {
        List<Element> fieldMapElements = new ArrayList<Element>();
        
        fieldMapElements.add(createBlock(rows, cols, rowsPerPlot, startX, startY));
        fieldMapElements.addAll(createRanges(rows, cols, rowsPerPlot, startX, startY));
        fieldMapElements.addAll(createCells(data, rows, cols, rowsPerPlot, startX, startY));
        
        return fieldMapElements;
    }
    
    private List<String> generateFieldmapLabels(List<String> names, int reps, int entries, int rows, int cols, boolean isSerpentine) {
        List<String> fieldTexts = new ArrayList<String>();
        int noOfTrials = names.size();
        for (int i = 0; i < noOfTrials; i++) {
            for (int j = 0; j < reps; j++) {
                for (int k = 0; k < entries; k++) {
                    fieldTexts.add("Entry " + (k+1) + NEXT_LINE + "Rep " + (j+1) + NEXT_LINE + names.get(i));
                }
            }
        }        
        addPadding(fieldTexts, rows * cols);
        if (isSerpentine) {
            makeSerpentine(fieldTexts, rows, cols);
        }
        return fieldTexts;
    }

    private void makeSerpentine(List<String> data, int rows, int cols) {
        for (int y = 0; y < rows && y < Math.ceil((double) data.size() / cols); y++) {
            int start = y * cols;
            int end = y * cols + cols - 1;
            if ((y % 2) == 1) {
                reverse(data, start, end);
            }
        }
    }
    
    private static void reverse(List<String> data, int start, int end) {
        int iterations = (end - start + 1) / 2;
        int pad = end - data.size() + 1;
        if (pad > 0) {
            for (int i = 0; i < pad; i++) {
                data.add(EMPTY_CELL);
            }
        }
        for (int i = 0; i < iterations; i++) {
            Collections.swap(data, start + i, end - i);
        }
    }
    
    private Element createBlock(int rows, int cols, int rowsPerPlot, int startX, int startY) {
        Rectangle block = new Rectangle();
        block.setX(startX);
        block.setY(startY);
        int width = BLOCK_MARGIN_X * (cols + 1) + (RANGE_MARGIN_X * 2 + CELL_WIDTH) * cols;
        int noOfPlotsInRange = rows / rowsPerPlot;
        int height = BLOCK_MARGIN_Y * 2 + RANGE_MARGIN_Y * (rows + noOfPlotsInRange) + PLOT_MARGIN_Y * (noOfPlotsInRange -1) + CELL_HEIGHT * rows;
        block.setWidth(width);
        block.setHeight(height);
        block.setFill("white");
        block.setStroke("red");
        return block;
    }
    
    private List<Element> createRanges(int rows, int cols, int rowsPerPlot, int startX, int startY) {
        List<Element> ranges = new ArrayList<Element>();
        startX += BLOCK_MARGIN_X;
        int noOfPlotsInRange = rows / rowsPerPlot;
        int width = RANGE_MARGIN_X * 2 + CELL_WIDTH;
        int height = RANGE_MARGIN_Y * (rows + noOfPlotsInRange) + PLOT_MARGIN_Y * (noOfPlotsInRange - 1) + CELL_HEIGHT * rows;
        int y = startY + BLOCK_MARGIN_Y;
        for (int i = 0; i < cols; i++) {
            Rectangle range = new Rectangle();
            range.setX(startX + (BLOCK_MARGIN_X + width) * i);
            range.setY(y);
            range.setWidth(width);
            range.setHeight(height);
            range.setFill("white");
            range.setStroke("blue");
            ranges.add(range);
        }
        return ranges;
    }
    
    private List<Element> createCells(List<String> data, int rows, int cols, int rowsPerPlot, int startX, int startY) {
        List<Element> cells = new ArrayList<Element>();
        startX += BLOCK_MARGIN_X + RANGE_MARGIN_X;
        startY += BLOCK_MARGIN_Y + RANGE_MARGIN_Y;
        int cellDistanceX = RANGE_MARGIN_X * 2 + BLOCK_MARGIN_X;
        int cellDistanceY = RANGE_MARGIN_Y;
        int plotDistanceY = RANGE_MARGIN_Y + PLOT_MARGIN_Y;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (data == null || i * cols + j < data.size()) {
                    int x = startX + (cellDistanceX + CELL_WIDTH) * j;
                    int y = startY + (cellDistanceY + CELL_HEIGHT) * i + plotDistanceY * (i / rowsPerPlot);
                    
                    Rectangle rect = new Rectangle();
                    if (data != null) {
                        rect.setTitle(data.get(i * cols + j));
                    }
                    rect.setId(CELL_ID_PREFIX + i + "_" + j);
                    rect.setStroke("black");
                    rect.setWidth(CELL_WIDTH);
                    rect.setHeight(CELL_HEIGHT);
                    rect.setX(x);
                    rect.setY(y);
                    cells.add(rect);
                    
                    /*Text text = new Text();
                    text.setX(x);
                    text.setY(y);
                    text.setText(data.get(i * cols + j));
                    cells.add(text);*/
                }
            }
        }
        return cells;
    }
    
    private void createPlots(List<Element> dataElements) {
        
    }
    
    private void addPadding(List<String> data, int expectedSize) {
        int dataSize = data.size();
        if (dataSize < expectedSize) {
            int numberOfPads = expectedSize - dataSize;
            for (int i = 0; i < numberOfPads; i++) {
                data.add(EMPTY_CELL);
            }
        }
    }
}
