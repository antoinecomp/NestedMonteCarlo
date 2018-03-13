package test;
/*
 * Copyright (c) 1998-2008 ChemAxon Ltd. All Rights Reserved.
 */

import chemaxon.struc.Molecule;
import chemaxon.marvin.MolPrinter;
import chemaxon.marvin.sketch.swing.SketchPanel;
import chemaxon.marvin.beans.MViewPane;
import chemaxon.formats.MolImporter;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;


/**
 * Application using MViewPane as the cell renderer and the cell 
 * editor of a JTable, and as an alternative approach using
 * MolPrinter as a cell renderer.
 * <p>
 * These renderer and editor classes are also accessible as part of the public API.<br>
 * Renderer: {@link chemaxon.marvin.beans.MViewRenderer}<br>
 * Editor: {@link chemaxon.marvin.beans.MViewEditor}
 * <p>
 * For the detailed description of this example, please visit
 * http://www.chemaxon.com/marvin/examples/beans/view-jtable/index.html
 *
 * @author  Judit Vasko-Szedlar
 * @author  Gabor Bartha
 * @version 5.0.3 Apr 16, 2008
 * @since   Marvin 2.10.5, 09/02/2002
 */
public class ViewJTable extends JPanel {

    private JTable table;

    /**
     * Constructor, creates the JTable and its editor and renderer objects
     */
    public ViewJTable() {
        //Creates the data object of the table
	MoleculeTableModel molModel = new MoleculeTableModel();
	//Creates the table
        table = new JTable(molModel);
	//Sets table's dimensions
        table.setPreferredScrollableViewportSize(new Dimension(1000, 800));
	table.setRowHeight(200);
	table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);	
	
	//Sets the cell renderer of the table
	setUpMolRenderer(table);
 	//Sets the cell editor of the table
        setUpMolEditor(table);

        //Creates a scroll pane and adds the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setViewportView(table);
        add(scrollPane);
    }

    /**
     * Create the GUI and show it. For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     * @param mols molecules to be displayed in the table
     */
//    public static void createAndShowGUI(Molecule[][] mols) {
    public static void createAndShowGUI(Object[][] mols) {
        //Create and set up the window.
        JFrame frame = new JFrame();
        frame.setTitle("MarvinView in JTable Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ViewJTable viewJTable = new ViewJTable();
        viewJTable.setMolecules(mols);

        //Adds a scroll pane to the content pane.
        frame.getContentPane().add(viewJTable, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

//    private void setMolecules(Molecule[][] mols) {
    private void setMolecules(Object[][] mols) {
        MoleculeTableModel tm = (MoleculeTableModel)table.getModel();
        tm.setMolecules(mols);
        table.setModel(tm);

        TableColumn column = null;
        for (int i = 0; i < tm.getColumnCount(); i++) {
        	column = table.getColumnModel().getColumn(i);
        	if (i == 0) {
        		column.setPreferredWidth(1); //third column is bigger
        	} else if (i == 1) {
        		column.setPreferredWidth(10); //third column is bigger
        	}{
        		column.setPreferredWidth(100);
        	}
        }
    }

    /**
     * Initializes an MViewPane instance and assigns it as the cell editor
     * of Molecule objects in the table.
     * @param table The JTable object to which an MViewPane
     * is set as a cell editor
     */
    private void setUpMolEditor(JTable table) {

        MViewEditor me = new MViewEditor();
	//Sets its editable property to MViewPane.SKETCHABLE which launches
        //MarvinSketch when double clicking on MViewPane
        me.setEditable(MViewPane.SKETCHABLE);
        //Sets the moleditor to the cell editor of the table which cells
        // are containing Molecule objects
	table.setDefaultEditor(Molecule.class, me);
    }

    /**
     * Initializes an MViewPane or MolPrinter instance and assigns it
     * as the cell renderer of Molecule objects in the table.
     * It also initializes and sets a renderer component to String objects.
     * @param table The JTable object to which an MViewPane
     * is set as a cell renderer
     */
    private void setUpMolRenderer(JTable table) {
        //using cell renderer with MViewPane
        table.setDefaultRenderer(Molecule.class, new MViewRenderer());

        // using cell renderer with MolPrinter
        //table.setDefaultRenderer(Molecule.class, new ViewRenderer());

        table.setDefaultRenderer(String.class, new StringRenderer());
    }

    public static void main(String[] args) {
        final Molecule[][] mols;
        if(args==null || args.length==0) {
            mols = new Molecule[4][2];
            try {
                mols[0][0] = MolImporter.importMol("C1CCCC1");
                mols[1][0] = MolImporter.importMol("C1=CC2=C(C=C1)C=CC=C2");
                mols[2][0] = MolImporter.importMol("CN1C=NC2=C1C(=O)N(C)C(=O)N2C");
                mols[3][0] = MolImporter.importMol("N1C=CC=C1");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            ArrayList molList = new ArrayList();
            try {
                MolImporter mi = new MolImporter(args[0]);
                Molecule mol;
                while((mol = mi.read())!=null) {
                    molList.add(mol);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            mols = new Molecule[molList.size()][2];
            for(int i = 0; i < mols.length; i++) {
                mols[i][0] = (Molecule)molList.get(i);
            }
        }
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(mols);
            }
        });
    }

    /**
     * String renderer to align text in cells
     */
    private static class StringRenderer extends DefaultTableCellRenderer {

        public StringRenderer() {
            setHorizontalAlignment( javax.swing.SwingConstants.CENTER );
        }
    }

    /**
     * MViewRenderer is a TableCellRenderer component that can be used to render
     * Molecule objects in JTables.
     */
    private static class MViewRenderer extends MViewPane
            implements TableCellRenderer {

        private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        /**
         * Creates the cell renderer. It can be assigned to JTables for example with
         * <code>table.setDefaultRenderer(Molecule.class, new MViewRenderer());</code>
         */
        public MViewRenderer() {
            super();
            setOpaque(true);
        }

        /**
         * The implementation of this method sets up the rendering component to
         * display the passed-in molecule, and then returns the component.
         * @param	table		the <code>JTable</code> that is asking the
         *				renderer to draw; can be <code>null</code>
         * @param	value		the value of the cell to be rendered; it is
         *				considered to be a {@link Molecule} instance
         * @param	isSelected	true if the cell is to be rendered with the
         *				selection highlighted; otherwise false
         * @param	hasFocus	if true, a special border is put on the cell, if
         *				the cell can be edited, it is rendered in the color used
         *				to indicate editing
         * @param	row	        the row index of the cell being drawn.  When
         *				drawing the header, the value of
         *				<code>row</code> is -1
         * @param	column	        the column index of the cell being drawn
         * @return MViewPane component that is configured to draw the molecule.
         */
	public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.setBackground(isSelected
                    ? table.getSelectionBackground()
                    : table.getBackground());
            setMolbg( isSelected
                    ? table.getSelectionBackground()
                    : table.getBackground());
            // Setting borders.
            if(hasFocus) {
                setBorder( UIManager.getBorder(
                        "Table.focusCellHighlightBorder") );
                if(table.isCellEditable(row, column)) {
                    setForeground( UIManager.getColor(
                            "Table.focusCellForeground") );
                    setBackground( UIManager.getColor(
                            "Table.focusCellBackground") );
                }
            } else {
                setBorder(noFocusBorder);
            }
            setM( 0, (Molecule)value );
            return this;
        }

    }

    /**
     * The cell renderer based on MolPrinter
     */
    private static class ViewRenderer extends JPanel
            implements TableCellRenderer {
        private static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
        private Dimension size;
        private Rectangle bounds;
        private MolPrinter printer;

        public ViewRenderer() {
            setOpaque(true);
            bounds=new Rectangle();
            size=new Dimension();
            printer=new MolPrinter();
        }

        public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
            printer.setBackgroundColor(isSelected
                    ? table.getSelectionBackground()
                    : table.getBackground());

            // Setting borders.
            if(hasFocus) {
                setBorder( UIManager.getBorder(
                        "Table.focusCellHighlightBorder") );
                if(table.isCellEditable(row, column)) {
                    super.setForeground( UIManager.getColor(
                            "Table.focusCellForeground") );
                    super.setBackground( UIManager.getColor(
                            "Table.focusCellBackground") );
                }
            } else {
                setBorder(noFocusBorder);
            }
            // Passing the current molecule to MolPrinter.
            printer.setMol((Molecule)value);
            return this;
        }

        public void paintComponent(Graphics g) {
            // Store the current size in bounds to give to MolPrinter.
            bounds.setSize(getSize(size));
            // It is very important to set the scale factor of MolPrinter,
            // otherwise the image will not appear.
            // The scale factor is computed by MolPrinter from
            // the current size.
            double scale = printer.maxScale(size);
            if( scale > SketchPanel.DEFAULT_SCALE ) {
                scale = SketchPanel.DEFAULT_SCALE;
            }
            printer.setScale(scale);
            // When MolPrinter is properly initialized, it can paint the
            // molecule.
            printer.paint(g, bounds);
        }
    }

    /**
     * MViewEditor is a DefaultCellEditor component that can be used to assign cell
     * editor to Molecule objects in JTables.
     */
    private static class MViewEditor extends DefaultCellEditor {
	//The current molecule of the MViewPane where MViewPane is the
	//cell editor
	private Molecule currentMol = null;

        /**
         * Creates the cell editor. It can be assigned to JTables for example with
         * <code>table.setDefaultEditor(Molecule.class, new MViewEditor());</code>
         */
        public MViewEditor() {
            super(new JCheckBox());
            //Sets the MViewPane to the editor component
            editorComponent = new MViewPane();
        }

        /**
         * Returns the reference to the editor component.
         * @return the editor component as <code>MViewPane</code>
         */
        public MViewPane getEditorComponent() {
            return (MViewPane)editorComponent;
        }


        /**
         * Returns the mode that determines if the structure is editable.
         * @return {@link MViewPane#VIEW_ONLY} (0) if molecules can be viewed only,
         *         {@link MViewPane#EDITABLE} (1) if they are editable with MarvinView,
         *         {@link MViewPane#SKETCHABLE} (2) if they are editable with MarvinSketch.
         */
        public int getEditable() {
            return getEditorComponent().getEditable();
        }

        /**
         * Sets the mode that determines if the structure is editable.
         * If the structure is allowed to be edited, the Edit > Structure menu or the double
         * mouse click performs the editing.
         * <p>
         * {@link MViewPane#VIEW_ONLY}: editing is disabled,<br>
         * {@link MViewPane#EDITABLE}: editing is enabled and launches MarvinView in a new window,<br>
         * {@link MViewPane#SKETCHABLE}: editing is enabled and launches MarvinSketch in a new window.
         * @param e   identifier of the mode
         */
        public void setEditable(int e) {
            getEditorComponent().setEditable(e);
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        /**
         * Returns the edited molecule that is a {@link Molecule} instance.
         */
        public Object getCellEditorValue() {
            currentMol = getEditorComponent().getM(0);
            return currentMol;
        }

        /**
         * Sets up the editor component.
         * @param	table		the <code>JTable</code> that is asking the
         *				editor to edit; can be <code>null</code>
         * @param	value		the value of the cell to be edited; it is
         *				considered to be a {@link Molecule} instance;
         *                          <code>null</code> is a valid value
         * @param	isSelected	true if the cell is to be rendered with
         *				highlighting
         * @param	row     	the row of the cell being edited
         * @param	column  	the column of the cell being edited
         * @return	the component for editing
         */
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
            currentMol = (Molecule)value;
            MViewPane mviewPane = getEditorComponent();

            mviewPane.setMolbg( table.getSelectionBackground());

            // Setting borders.
            mviewPane.setBorder( UIManager.getBorder(
                    "Table.focusCellHighlightBorder") );
            mviewPane.setForeground( UIManager.getColor(
                    "Table.focusCellForeground") );
            mviewPane.setBackground( UIManager.getColor(
                    "Table.focusCellBackground") );

            mviewPane.setM(0,currentMol);
            return editorComponent;
        }
    }

    /**
     * The table model object manages the actual table data. 
     */
    private static class MoleculeTableModel extends AbstractTableModel {

	//Defines names of columns
        final String[] columnNames = {"Index", "ID", "Rxn", "Molecule"};//, "Molecule cxsmarts"};

        Object[][] data = {};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

   	//Gets a molecule located at the defined row and col of the table
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

	// In case of double clicking with the mouse's left button
        // on a cell, this function gets called.
        // It returns true for the molecule column only,
        // thus allowing its cell editor to work.
        public boolean isCellEditable(int row, int col) {
            return col == 1 || col==2;
        }

	//Sets a molecule at the defined row and col of the table
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
//            if(col==2) {
//                // setting the mass here keeps the column updated
//                // upon editing the molecules
////                setValueAt(""+((Molecule)value).getMass(), row, 2);
//                setValueAt(""+((Molecule)value).toFormat("cxsmarts"), row, 3);
//            }
            fireTableCellUpdated(row, col);
        }

//        public void setMolecules(Molecule[][] mols) {
        public void setMolecules(Object[][] mols) {
            data = new Object[mols.length][4];
            for(int i=0; i<mols.length; i++) {
                setValueAt(""+(i+1), i, 0);
                setValueAt(mols[i][0], i, 1);
                setValueAt(mols[i][1], i, 2);
                setValueAt(mols[i][2], i, 3);
//                setValueAt(((Molecule)mols[i][1]).toFormat("cxsmarts"), i, 3);
                // we set the mass inside the setValueAt method instead
                //setValueAt(""+mols[i].getMass(), i, 2);
            }
        }

    }

}