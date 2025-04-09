package de.saring.sportstracker.gui.views.listviews;

import de.saring.sportstracker.data.Note;
import de.saring.sportstracker.gui.STContext;
import de.saring.sportstracker.gui.STDocument;
import de.saring.sportstracker.gui.views.ViewPrinter;
import de.saring.util.StringUtils;
import de.saring.util.data.IdObject;
import de.saring.util.gui.javafx.LocalDateCellFactory;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller class of the Note List View, which displays all the user notes  (or a filtered list) in a table view.
 *
 * @author Stefan Saring
 */
@Singleton
public class NoteListViewController extends AbstractListViewController<Note> {

    private static final Logger LOGGER = Logger.getLogger(NoteListViewController.class.getName());

    @FXML
    private TableView<Note> tvNotes;

    @FXML
    private TableColumn<Note, LocalDateTime> tcDate;
    @FXML
    private TableColumn<Note, Object> tcSportType;
    @FXML
    private TableColumn<Note, Object> tcEquipment;
    @FXML
    private TableColumn<Note, String> tcComment;

    /**
     * Standard c'tor for dependency injection.
     *
     * @param context the SportsTracker UI context
     * @param document the SportsTracker document / model
     * @param viewPrinter the printer of the SportsTracker views
     */
    @Inject
    public NoteListViewController(final STContext context, final STDocument document, final ViewPrinter viewPrinter) {
        super(context, document, viewPrinter);
    }

    @Override
    public ViewType getViewType() {
        return ViewType.NOTE_LIST;
    }

    @Override
    public int getSelectedNoteCount() {
        return getSelectedEntryCount();
    }

    @Override
    public long[] getSelectedNoteIDs() {
        return getSelectedEntryIDs();
    }

    @Override
    public void selectEntry(final IdObject entry) {
        if (entry != null && entry instanceof Note note) {
            selectAndScrollToEntry(note);
        }
    }

    @Override
    protected String getFxmlFilename() {
        return "/fxml/views/NoteListView.fxml";
    }

    @Override
    protected TableView<Note> getTableView() {
        return tvNotes;
    }

    @Override
    protected void setupTableColumns() {

        // setup factories for providing cell values
        tcDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        tcSportType.setCellValueFactory(cellData -> new SimpleObjectProperty<>( //
                cellData.getValue().getSportType() == null ? null : cellData.getValue().getSportType().getName()));
        tcEquipment.setCellValueFactory(cellData -> new SimpleObjectProperty<>( //
                cellData.getValue().getEquipment() == null ? null : cellData.getValue().getEquipment().getName()));
        tcComment.setCellValueFactory(cellData -> new SimpleStringProperty( //
                StringUtils.getFirstLineOfText(cellData.getValue().getComment())));

        // setup custom factories for displaying cells
        tcDate.setCellFactory(new LocalDateCellFactory<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setupDefaultSorting() {
        // default sort order is by date descending
        tcDate.setSortType(TableColumn.SortType.DESCENDING);
        tvNotes.getSortOrder().addAll(tcDate);
    }

    @Override
    protected List<Note> getTableEntries() {
        return getDocument().getFilterableNoteList().stream().toList();
    }
}
