package jrm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import jrm.Messages;
import jrm.profile.Export;
import jrm.profile.Profile;
import jrm.profile.ProfileNFOMame;
import jrm.profile.Export.ExportType;
import jrm.profile.ProfileNFOMame.MameStatus;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareListList;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.EntityStatus;
import jrm.profile.data.Machine;
import jrm.profile.data.Software;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ProfileViewer extends JDialog
{
	private JTable tableEntity;
	private JTable tableW;
	private JTable tableWL;
	private JTextField txtSearch;

	public ProfileViewer(Window owner, Profile profile)
	{
		super(owner);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ProfileViewer.class.getResource("/jrm/resources/rom.png")));
		setTitle(Messages.getString("ProfileViewer.this.title")); //$NON-NLS-1$

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panelWare = new JPanel();
		splitPane.setLeftComponent(panelWare);
		panelWare.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPaneWLW = new JSplitPane();
		splitPaneWLW.setOneTouchExpandable(true);
		splitPaneWLW.setContinuousLayout(true);
		splitPaneWLW.setResizeWeight(0.25);
		panelWare.add(splitPaneWLW, BorderLayout.CENTER);

		tableEntity = new JTable();
		tableEntity.setPreferredScrollableViewportSize(new Dimension(1200, 300));
		tableEntity.setShowGrid(false);
		tableEntity.setShowHorizontalLines(false);
		tableEntity.setShowVerticalLines(false);
		tableEntity.setRowSelectionAllowed(false);
		tableEntity.setFillsViewportHeight(true);
		tableEntity.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JPanel panelW = new JPanel();
		splitPaneWLW.setRightComponent(panelW);
		// panelWare.add(panelSL, BorderLayout.NORTH);
		panelW.setLayout(new BorderLayout(0, 0));

		JToolBar toolBarW = new JToolBar();
		panelW.add(toolBarW, BorderLayout.SOUTH);

		JToggleButton tglbtnMissingW = new JToggleButton("");
		tglbtnMissingW.setSelected(true);
		tglbtnMissingW.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingW.toolTipText")); //$NON-NLS-1$
		tglbtnMissingW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_red.png")));
		toolBarW.add(tglbtnMissingW);

		JToggleButton tglbtnPartialW = new JToggleButton("");
		tglbtnPartialW.setSelected(true);
		tglbtnPartialW.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialW.toolTipText")); //$NON-NLS-1$
		tglbtnPartialW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_orange.png")));
		toolBarW.add(tglbtnPartialW);

		JToggleButton tglbtnCompleteW = new JToggleButton("");
		tglbtnCompleteW.setSelected(true);
		tglbtnCompleteW.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/folder_closed_green.png")));
		tglbtnCompleteW.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteW.toolTipText")); //$NON-NLS-1$
		toolBarW.add(tglbtnCompleteW);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(null);
		toolBarW.add(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 286, 166, 0 };
		gbl_panel_1.rowHeights = new int[] { 20, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel lblSearch = new JLabel(Messages.getString("ProfileViewer.lblSearch.text")); //$NON-NLS-1$
		GridBagConstraints gbc_lblSearch = new GridBagConstraints();
		gbc_lblSearch.insets = new Insets(0, 0, 0, 5);
		gbc_lblSearch.anchor = GridBagConstraints.EAST;
		gbc_lblSearch.gridx = 0;
		gbc_lblSearch.gridy = 0;
		panel_1.add(lblSearch, gbc_lblSearch);

		txtSearch = new JTextField();
		txtSearch.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				String search = txtSearch.getText();
				@SuppressWarnings("unchecked")
				int row = ((AnywareList<Anyware>) tableW.getModel()).find(search);
				if(row >= 0)
				{
					tableW.setRowSelectionInterval(row, row);
					tableW.scrollRectToVisible(tableW.getCellRect(row, 0, true));
				}
			}
		});
		GridBagConstraints gbc_txtSearch = new GridBagConstraints();
		gbc_txtSearch.fill = GridBagConstraints.VERTICAL;
		gbc_txtSearch.anchor = GridBagConstraints.WEST;
		gbc_txtSearch.gridx = 1;
		gbc_txtSearch.gridy = 0;
		panel_1.add(txtSearch, gbc_txtSearch);
		txtSearch.setText("");
		txtSearch.setColumns(20);

		tglbtnMissingW.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected());
			}
		});
		tglbtnPartialW.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected());
			}
		});
		tglbtnCompleteW.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterW(tglbtnMissingW.isSelected(), tglbtnPartialW.isSelected(), tglbtnCompleteW.isSelected());
			}
		});

		JScrollPane scrollPaneW = new JScrollPane();
		panelW.add(scrollPaneW);

		tableW = new JTable();
		scrollPaneW.setViewportView(tableW);
		tableW.setPreferredScrollableViewportSize(new Dimension(600, 400));
		tableW.setFillsViewportHeight(true);
		tableW.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableW.setShowGrid(false);
		tableW.setShowHorizontalLines(false);
		tableW.setShowVerticalLines(false);
		tableW.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JPanel panel = new JPanel();
		splitPaneWLW.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPaneWL = new JScrollPane();
		panel.add(scrollPaneWL);

		tableWL = new JTable();
		scrollPaneWL.setViewportView(tableWL);
		tableWL.setPreferredScrollableViewportSize(new Dimension(300, 400));
		tableWL.setModel(profile.machinelist_list);
		tableWL.setTableHeader(new JTableHeader(tableWL.getColumnModel())
		{
			public String getToolTipText(MouseEvent e)
			{
				return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
			}
		});
		tableWL.setFillsViewportHeight(true);
		tableWL.setShowGrid(false);
		tableWL.setShowHorizontalLines(false);
		tableWL.setShowVerticalLines(false);
		tableWL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}
		});
		addPopup(tableWL, popupMenu);
		
		JMenu mnExportAll = new JMenu(Messages.getString("ProfileViewer.mnExportAll.text")); //$NON-NLS-1$
		popupMenu.add(mnExportAll);
		
		JMenuItem mntmAllAsMameDat = new JMenuItem("as Mame dat");
		mntmAllAsMameDat.setEnabled(false);
		mnExportAll.add(mntmAllAsMameDat);
		
		JMenuItem mntmAllAsSoftwareLists = new JMenuItem("as Software Lists dat");
		mntmAllAsSoftwareLists.setEnabled(false);
		mnExportAll.add(mntmAllAsSoftwareLists);
		
		JMenuItem mntmAllAsLogiqxDat = new JMenuItem("as Logiqx dat");
		mntmAllAsLogiqxDat.setEnabled(false);
		mnExportAll.add(mntmAllAsLogiqxDat);
		
		JMenu mnExportFiltered = new JMenu(Messages.getString("ProfileViewer.mnExportFiltered.text")); //$NON-NLS-1$
		popupMenu.add(mnExportFiltered);
		
		JMenuItem mntmFilteredAsMameDat = new JMenuItem("as Mame dat");
		mntmFilteredAsMameDat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export(ExportType.MAME);
			}
		});
		mnExportFiltered.add(mntmFilteredAsMameDat);
		
		JMenuItem mntmFilteredAsSoftwareLists = new JMenuItem("as Software Lists dat");
		mntmFilteredAsSoftwareLists.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export(ExportType.SOFTWARELIST);
			}
		});
		mnExportFiltered.add(mntmFilteredAsSoftwareLists);
		
		JMenuItem mntmFilteredAsLogiqxDat = new JMenuItem("as Logiqx dat");
		mntmFilteredAsLogiqxDat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export(ExportType.DATAFILE);
			}
		});
		mnExportFiltered.add(mntmFilteredAsLogiqxDat);
		
		JMenu mnExportSelected = new JMenu(Messages.getString("ProfileViewer.mnExportSelected.text")); //$NON-NLS-1$
		popupMenu.add(mnExportSelected);
		
		JMenuItem mntmSelectedAsMameDat = new JMenuItem("as Mame dat");
		mntmSelectedAsMameDat.setEnabled(false);
		mnExportSelected.add(mntmSelectedAsMameDat);
		
		JMenuItem mntmSelectedAsSoftwareLists = new JMenuItem("as Software Lists dat");
		mntmSelectedAsSoftwareLists.setEnabled(false);
		mnExportSelected.add(mntmSelectedAsSoftwareLists);
		
		JMenuItem mntmSelectedAsLogiqxDat = new JMenuItem("as Logiqx dat");
		mntmSelectedAsLogiqxDat.setEnabled(false);
		mnExportSelected.add(mntmSelectedAsLogiqxDat);

		JToolBar toolBarWL = new JToolBar();
		panel.add(toolBarWL, BorderLayout.SOUTH);

		JToggleButton tglbtnMissingWL = new JToggleButton("");
		tglbtnMissingWL.setSelected(true);
		tglbtnMissingWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_red.png")));
		tglbtnMissingWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnMissingWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnMissingWL);

		JToggleButton tglbtnPartialWL = new JToggleButton("");
		tglbtnPartialWL.setSelected(true);
		tglbtnPartialWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_orange.png")));
		tglbtnPartialWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnPartialWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnPartialWL);

		JToggleButton tglbtnCompleteWL = new JToggleButton("");
		tglbtnCompleteWL.setSelected(true);
		tglbtnCompleteWL.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/disk_multiple_green.png")));
		tglbtnCompleteWL.setToolTipText(Messages.getString("ProfileViewer.tglbtnCompleteWL.toolTipText")); //$NON-NLS-1$
		toolBarWL.add(tglbtnCompleteWL);

		tglbtnMissingWL.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected());
			}
		});
		tglbtnPartialWL.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected());
			}
		});
		tglbtnCompleteWL.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterWL(tglbtnMissingWL.isSelected(), tglbtnPartialWL.isSelected(), tglbtnCompleteWL.isSelected());
			}
		});

		tableWL.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					ListSelectionModel model = (ListSelectionModel) e.getSource();
					TableModel tablemodel = (TableModel) tableWL.getModel();
					if(model != null && tablemodel != null)
					{
						if(!model.isSelectionEmpty())
						{
							AnywareList<?> anywarelist = (AnywareList<?>) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
							anywarelist.reset();
							tableW.setModel(anywarelist);
							tableW.setTableHeader(new JTableHeader(tableW.getColumnModel())
							{
								public String getToolTipText(MouseEvent e)
								{
									return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
								}
							});
							if(tableW.getRowCount() > 0)
								tableW.setRowSelectionInterval(0, 0);
							for(int i = 0; i < tableW.getColumnModel().getColumnCount(); i++)
							{
								TableColumn column = tableW.getColumnModel().getColumn(i);
								column.setCellRenderer(anywarelist.getColumnRenderer(i));
								int width = anywarelist.getColumnWidth(i);
								if(width > 0)
								{
									column.setMinWidth(width / 2);
									column.setPreferredWidth(width);
								}
								else if(width < 0)
								{
									column.setMinWidth(-width);
									column.setMaxWidth(-width);
								}
							}
						}
						else
						{
							tableW.setModel(new DefaultTableModel());
							tableEntity.setModel(new DefaultTableModel());
						}
					}
				}
			}
		});
		tableW.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() == 2)
				{
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if(row >= 0)
					{
						AnywareList<?> tablemodel = (AnywareList<?>) target.getModel();
						int column = target.columnAtPoint(e.getPoint());
						Object obj = tablemodel.getValueAt(row, column);
						if(obj instanceof Anyware)
						{
							Anyware ware = (Anyware) obj;
							if(column > 1)
							{
								row = tablemodel.find(ware);
								if(row >= 0)
								{
									target.setRowSelectionInterval(row, row);
									target.scrollRectToVisible(target.getCellRect(row, 0, true));
								}
							}
							else if(ware.getStatus()==AnywareStatus.COMPLETE)
							{
								if(Profile.curr_profile != null)
								{
									Profile profile = Profile.curr_profile;
									if(profile.nfo.mame.getStatus() == MameStatus.UPTODATE)
									{
										ProfileNFOMame mame = profile.nfo.mame;
										String[] args = null;
										if(ware instanceof Software)
										{
											List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty("roms_dest_dir", "")));
											if(profile.getProperty("swroms_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("swroms_dest_dir", ""));
											if(profile.getProperty("disks_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("disks_dest_dir", ""));
											if(profile.getProperty("swdisks_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("swdisks_dest_dir", ""));
											System.out.println(((Software)ware).sl.name+", "+((Software)ware).compatibility);
											Machine machine = profile.machinelist_list.findMachine(((Software)ware).sl.name, ((Software)ware).compatibility); 
											if(machine!=null)
											{
												System.out.println("-> "+machine.name+" "+ware.name);
												args = new String[] {mame.getFile().getAbsolutePath(), machine.name, ware.name, "-homepath", mame.getFile().getParent(), "-rompath", rompaths.stream().collect(Collectors.joining(";"))};
											}
										}
										else
										{
											List<String> rompaths = new ArrayList<>(Collections.singletonList(profile.getProperty("roms_dest_dir", "")));
											if(profile.getProperty("disks_dest_dir_enabled", false))
												rompaths.add(profile.getProperty("disks_dest_dir", ""));
											args = new String[] {mame.getFile().getAbsolutePath(), ware.name, "-homepath", mame.getFile().getParent(), "-rompath", rompaths.stream().collect(Collectors.joining(";"))};
										}
										if(args != null)
										{
											System.out.println(Arrays.asList(args).stream().collect(Collectors.joining(" ")));
											ProcessBuilder pb = new ProcessBuilder(args).directory(mame.getFile().getParentFile()).redirectErrorStream(true).redirectOutput(new File(mame.getFile().getParentFile(), "JRomManager.log"));
											try
											{
												pb.start().waitFor();
											}
											catch(InterruptedException | IOException e1)
											{
												//TODO show error dialog
												e1.printStackTrace();
											}
										}
									}
									else
									{
										System.out.println(profile.nfo.mame.getStatus());										
									}
								}
								else
								{
									System.out.println("no profile");
								}
							}
							else
							{
								//TODO show error dialog
								System.out.println(ware.getStatus());
							}
						}
					}
				}
			}
		});
		tableW.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(!e.getValueIsAdjusting())
				{
					ListSelectionModel model = (ListSelectionModel) e.getSource();
					TableModel tablemodel = (TableModel) tableW.getModel();
					if(model != null && tablemodel != null)
					{
						if(!model.isSelectionEmpty())
						{
							Anyware anyware = (Anyware) tablemodel.getValueAt(model.getMinSelectionIndex(), 0);
							anyware.reset();
							tableEntity.setModel(anyware);
							tableEntity.setTableHeader(new JTableHeader(tableEntity.getColumnModel())
							{
								public String getToolTipText(MouseEvent e)
								{
									return columnModel.getColumn(columnModel.getColumnIndexAtX(e.getPoint().x)).getHeaderValue().toString();
								}
							});
							for(int i = 0; i < tableEntity.getColumnModel().getColumnCount(); i++)
							{
								TableColumn column = tableEntity.getColumnModel().getColumn(i);
								column.setCellRenderer(Anyware.getColumnRenderer(i));
								int width = Anyware.getColumnWidth(i);
								if(width > 0)
								{
									column.setMinWidth(width / 2);
									column.setPreferredWidth(width);
								}
								else if(width < 0)
								{
									Component component = column.getCellRenderer().getTableCellRendererComponent(tableEntity, null, false, false, 0, i);
									int pixwidth = component.getFontMetrics(component.getFont()).stringWidth(String.format("%0" + (-width) + "d", 0));
									column.setMinWidth(pixwidth / 2);
									column.setPreferredWidth(pixwidth);
									column.setMaxWidth(pixwidth);
								}
							}
						}
						else
						{
							tableEntity.setModel(new DefaultTableModel());
						}
					}
				}
			}
		});

		JPanel panelEntity = new JPanel();
		splitPane.setRightComponent(panelEntity);
		panelEntity.setLayout(new BorderLayout(0, 0));

		JToolBar toolBarEntity = new JToolBar();
		panelEntity.add(toolBarEntity, BorderLayout.SOUTH);

		JToggleButton tglbtnBad = new JToggleButton("");
		tglbtnBad.setSelected(true);
		tglbtnBad.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_red.png")));
		tglbtnBad.setToolTipText(Messages.getString("ProfileViewer.tglbtnBad.toolTipText")); //$NON-NLS-1$
		toolBarEntity.add(tglbtnBad);

		JToggleButton tglbtnOK = new JToggleButton("");
		tglbtnOK.setSelected(true);
		tglbtnOK.setToolTipText(Messages.getString("ProfileViewer.tglbtnOK.toolTipText")); //$NON-NLS-1$
		tglbtnOK.setIcon(new ImageIcon(ProfileViewer.class.getResource("/jrm/resources/icons/bullet_green.png")));
		toolBarEntity.add(tglbtnOK);

		tglbtnBad.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected());
			}
		});
		tglbtnOK.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				setFilterE(tglbtnBad.isSelected(), tglbtnOK.isSelected());
			}
		});

		JScrollPane scrollPaneEntity = new JScrollPane();
		panelEntity.add(scrollPaneEntity, BorderLayout.CENTER);

		scrollPaneEntity.setViewportView(tableEntity);

		reset(profile);
		pack();
		setVisible(true);
	}

	private void export(ExportType type)
	{
		final Progress progress = new Progress(ProfileViewer.this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>()
		{

			@Override
			protected Void doInBackground() throws Exception
			{
				new Export(Profile.curr_profile, new File("dat.xml"), type, progress);
				return null;
			}

			@Override
			protected void done()
			{
				progress.dispose();
			}

		};
		worker.execute();
		progress.setVisible(true);
	}

	public void setFilterWL(boolean missing, boolean partial, boolean complete)
	{
		EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if(missing)
			filter.add(AnywareStatus.MISSING);
		if(partial)
			filter.add(AnywareStatus.PARTIAL);
		if(complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareListList<?>) tableWL.getModel()).setFilter(filter);
		if(tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	public void setFilterW(boolean missing, boolean partial, boolean complete)
	{
		EnumSet<AnywareStatus> filter = EnumSet.of(AnywareStatus.UNKNOWN);
		if(missing)
			filter.add(AnywareStatus.MISSING);
		if(partial)
			filter.add(AnywareStatus.PARTIAL);
		if(complete)
			filter.add(AnywareStatus.COMPLETE);
		((AnywareList<?>) tableW.getModel()).setFilter(filter);
		if(tableW.getRowCount() > 0)
			tableW.setRowSelectionInterval(0, 0);
	}

	public void setFilterE(boolean ko, boolean ok)
	{
		EnumSet<EntityStatus> filter = EnumSet.of(EntityStatus.UNKNOWN);
		if(ko)
			filter.add(EntityStatus.KO);
		if(ok)
			filter.add(EntityStatus.OK);
		((Anyware) tableEntity.getModel()).setFilter(filter);
		if(tableEntity.getRowCount() > 0)
			tableEntity.setRowSelectionInterval(0, 0);
	}

	public void clear()
	{
		tableEntity.setModel(new DefaultTableModel());
		tableW.setModel(new DefaultTableModel());
		tableWL.setModel(new DefaultTableModel());
	}

	public void reset(Profile profile)
	{
		AnywareListList<?> model = profile.machinelist_list;
		model.reset();
		tableWL.setModel(model);
		for(int i = 0; i < tableWL.getColumnModel().getColumnCount(); i++)
		{
			TableColumn column = tableWL.getColumnModel().getColumn(i);
			column.setCellRenderer(model.getColumnRenderer(i));
			int width = model.getColumnWidth(i);
			if(width > 0)
			{
				column.setMinWidth(width / 2);
				column.setPreferredWidth(width);
			}
			else if(width < 0)
			{
				column.setMinWidth(-width);
				column.setMaxWidth(-width);
			}
		}
		if(tableWL.getRowCount() > 0)
			tableWL.setRowSelectionInterval(0, 0);
	}

	public void reload()
	{
		TableModel tablemodel = (TableModel) tableWL.getModel();
		if(tablemodel instanceof AnywareListList<?>)
			((AnywareListList<?>) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareListList<?>) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = (TableModel) tableW.getModel();
		if(tablemodel instanceof AnywareList<?>)
			((AnywareList<?>) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((AnywareList<?>) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
		tablemodel = (TableModel) tableEntity.getModel();
		if(tablemodel instanceof Anyware)
			((Anyware) tablemodel).fireTableChanged(new TableModelEvent(tablemodel, 0, ((Anyware) tablemodel).getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
