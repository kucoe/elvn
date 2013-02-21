package net.kucoe.elvn.controls;

import net.kucoe.elvn.ListColor;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;


/**
 * Color chooser.
 * 
 * @author Vitaliy Basyuk
 */
public class ColorBox extends ComboBox<ListColor> {
    
    /**
     * Provides label for color
     * 
     * @author Vitaliy Basyuk
     */
    public interface ColorLabelProvider {
        /**
         * Returns label
         * 
         * @param color
         * @return string
         */
        String getLabel(ListColor color);
    }
    
    class ColorCell extends ListCell<ListColor> {
        
        private final ColorLabelProvider provider;
        
        public ColorCell(ColorLabelProvider colorLabelProvider) {
            provider = colorLabelProvider;
            if (provider == null) {
                setPrefWidth(20);
                setMaxWidth(20);
                setPrefHeight(25);
                setMaxHeight(25);
            }
        }
        
        @Override
        public void updateItem(final ListColor item, final boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                Rectangle rect = new Rectangle(20, 20);
                rect.setFill(Color.web(item.getHex()));
                rect.setArcHeight(10);
                rect.setArcWidth(10);
                setGraphic(rect);
                setText(provider == null ? null : provider.getLabel(item));
            } else {
                setGraphic(null);
                setText(null);
            }
            getStyleClass().add("color-chooser-cell");
        }
    }
    
    /**
     * Constructs ColorBox.
     * 
     * @param labelProvider {@link ColorLabelProvider}
     */
    public ColorBox(final ColorLabelProvider labelProvider) {
        setId("color-chooser");
        if (labelProvider == null) {
            setMaxWidth(35);
            setPrefWidth(35);
            setMaxHeight(35);
            setPrefHeight(35);
        }
        setButtonCell(new ColorCell(labelProvider));
        setCellFactory(new Callback<ListView<ListColor>, ListCell<ListColor>>() {
            @Override
            public ListCell<ListColor> call(final ListView<ListColor> param) {
                return new ColorCell(labelProvider);
            }
        });
    }
}
