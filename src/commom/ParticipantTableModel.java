package commom;

import java.util.List;


import javax.swing.table.AbstractTableModel;

import commom.model.Participant;

public class ParticipantTableModel extends AbstractTableModel {
	private List<Participant> data;
	private final String[] columnNames = { "ID", "Name"};

	public ParticipantTableModel(List<Participant> data) {
		this.data = data;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Participant test = data.get(rowIndex);
        switch (columnIndex) {
            case 0: 
                return test.getId();
            case 1: 
                return test.getName();
            default:
                return null; 
        }
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	public Participant getParticipantAt(int rowIndex) {
		return data.get(rowIndex);
	}
}