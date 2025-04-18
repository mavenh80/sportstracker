package de.saring.sportstracker.gui.dialogs;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import de.saring.sportstracker.data.Equipment;
import de.saring.sportstracker.data.SportType;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import de.saring.sportstracker.data.Note;

/**
 * This ViewModel class provides JavaFX properties of all Note attributes to be edited in the dialog.
 * So they can be bound to the appropriate dialog view controls.
 *
 * @author Stefan Saring
 */
public class NoteViewModel {

    private final Long id;
    public final ObjectProperty<LocalDate> date;
    public final ObjectProperty<LocalTime> time;
    public final ObjectProperty<SportType> sportType;
    public final ObjectProperty<Equipment> equipment;
    public final StringProperty comment;

    /**
     * Creates the NoteViewModel with JavaFX properties for the passed Note object.
     *
     * @param note Note to be edited
     */
    public NoteViewModel(final Note note) {
        this.id = note.getId();
        this.date = new SimpleObjectProperty<>(note.getDateTime().toLocalDate());
        this.time = new SimpleObjectProperty<>(note.getDateTime().toLocalTime());
        this.sportType = new SimpleObjectProperty<>(note.getSportType());
        this.equipment = new SimpleObjectProperty<>(note.getEquipment());
        this.comment = new SimpleStringProperty(note.getComment());
    }

    /**
     * Creates a new Note domain object from the edited JavaFX properties.
     *
     * @return Note
     */
    public Note getNote() {
        final Note note = new Note(id);
        note.setDateTime(LocalDateTime.of(date.get(), time.get()));
        note.setSportType(sportType.getValue());
        note.setEquipment(equipment.getValue());
        note.setComment(comment.getValue().trim());
        return note;
    }
}
