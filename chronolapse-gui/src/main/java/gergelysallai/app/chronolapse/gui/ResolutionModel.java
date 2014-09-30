package gergelysallai.app.chronolapse.gui;

import javax.swing.*;

public class ResolutionModel  extends AbstractListModel implements ComboBoxModel {

    Resolution[] data = {
            new Resolution("1080p",1920,1080),
            new Resolution("720p",1280, 720)
    };
    Resolution selection = data[0];

    @Override
    public void setSelectedItem(Object anItem) {
        for(Resolution item : data)
            if(item.name.equals((String)anItem)) {
                this.selection = item;
            }
    }

    @Override
    public String getSelectedItem() {
        return this.selection.name;
    }

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public String getElementAt(int index) {
        return data[index].name;
    }

    public Resolution getSelectedResolution() {
        return this.selection;
    }
}