package de.saring.sportstracker.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * This class contains all unit tests for the NoteList class.
 *
 * @author Stefan Saring
 */
public class NoteListTest {

    private NoteList list;
    private SportTypeList sportTypeList;

    /**
     * Setup of test data.
     */
    @BeforeEach
    public void setUp() {

        // create a sport type list with 2 sport types with 2 equipments for the first sport type
        sportTypeList = new SportTypeList();

        SportType type1 = new SportType(1L);
        type1.setName("SportType 1");
        Equipment equipment11 = new Equipment(11L);
        equipment11.setName("Equipment 11");
        type1.getEquipmentList().set(equipment11);
        Equipment equipment12 = new Equipment(12L);
        equipment12.setName("Equipment 12");
        type1.getEquipmentList().set(equipment12);
        sportTypeList.set(type1);

        SportType type2 = new SportType(2L);
        type2.setName("SportType 2");
        sportTypeList.set(type2);

        // create a new list with some test content
        list = new NoteList();

        Note note1 = new Note(1L);
        note1.setDateTime(LocalDateTime.of(2003, 9, 2, 0, 0, 0));
        note1.setSportType(type1);
        note1.setEquipment(equipment11);
        note1.setComment("Dummy note 1");
        list.set(note1);

        Note note2 = new Note(2L);
        note2.setDateTime(LocalDateTime.of(2003, 8, 20, 0, 0, 0));
        note2.setSportType(type1);
        note2.setEquipment(equipment12);
        note2.setComment("Dummy note 2");
        list.set(note2);

        Note note3 = new Note(3L);
        note3.setDateTime(LocalDateTime.of(2003, 9, 6, 0, 0, 0));
        note3.setComment("Dummy note 3");
        list.set(note3);
    }

    /**
     * Tests for getEntriesForFilter().
     */
    @Test
    public void testGetEntriesForFilter1() {

        // all 3 notes should be found
        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 2, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(3, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): no notes should be found (no notes in time span).
     */
    @Test
    public void testGetEntriesForFilter2() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 4, 30));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(0, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): all notes should be found (no notes in time span, but filter is set to type 
     * NOTE).
     */
    @Test
    public void testGetEntriesForFilter3() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 4, 30));
        filter.setEntryType(EntryFilter.EntryType.WEIGHT);
        filter.setCommentSubString("");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(3, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 2 notes should be found (in the specified time span).
     */
    @Test
    public void testGetEntriesForFilter4() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 9, 2));
        filter.setDateEnd(LocalDate.of(2003, 9, 6));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(2, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 3 notes should be found (with comment substring "NOTE").
     */
    @Test
    public void testGetEntriesForFilter5() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("NOTE");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(3, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 1 note should be found (with comment substring "OTE 2").
     */
    @Test
    public void testGetEntriesForFilter6() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString(" OTE 2 ");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(1, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 0 notes should be found (with comment substring "NotInThere").
     */
    @Test
    public void testGetEntriesForFilter7() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("NotInThere");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(0, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 2 notes should be found (with comment regular expression substring "ote [0-2]").
     */
    @Test
    public void testGetEntriesForFilter8() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("ote [0-2]");
        filter.setRegularExpressionMode(true);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(2, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 3 notes should be found (with comment regular expression substring for 3 small 
     * characters).
     */
    @Test
    public void testGetEntriesForFilter9() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("[a-z]{3}");
        filter.setRegularExpressionMode(true);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(3, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): 0 notes should be found (with comment regular expression substring for 8 small
     * characters).
     */
    @Test
    public void testGetEntriesForFilter10() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("[a-z]{8}");
        filter.setRegularExpressionMode(true);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(0, entryList.size());
    }

    /**
     * Tests for getEntriesForFilter(): use of regular expression "ote [0-2" with syntax error => PatternSyntaxException
     * needs to be thrown.
     */
    @Test
    public void testGetEntriesForFilter11() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString("cise [0-2");
        filter.setRegularExpressionMode(true);

        assertThrows(PatternSyntaxException.class, () ->
            list.getEntriesForFilter(filter));
    }

    /**
     * Tests for getEntriesForFilter() - scenario: the filter contains multiple words, all of them
     * need to be contained in the comment text, the case and the order does not matter (no regex-mode).
     * One single note needs to be found for this filter.
     */
    @Test
    public void testGetEntriesForFilter12() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setEntryType(EntryFilter.EntryType.NOTE);
        filter.setCommentSubString(" 3    NOTe ");
        filter.setRegularExpressionMode(false);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(1, entryList.size());
        assertEquals("Dummy note 3", entryList.getAt(0).getComment());
    }

    /**
     * Tests for getEntriesForFilter() - scenario: the filter contains the criteria for sport type 1.
     * Two notes need to be found for this filter.
     */
    @Test
    public void testGetEntriesForFilter13() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setSportType(sportTypeList.getByID(1));
        filter.setEntryType(EntryFilter.EntryType.NOTE);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(2, entryList.size());
        assertEquals(1, entryList.getAt(0).getId());
        assertEquals(2, entryList.getAt(1).getId());
    }

    /**
     * Tests for getEntriesForFilter() - scenario: the filter contains the criteria for sport type 1 and equipment 1.
     * One note need to be found for this filter.
     */
    @Test
    public void testGetEntriesForFilter14() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setSportType(sportTypeList.getByID(1));
        filter.setEquipment(sportTypeList.getByID(1).getEquipmentList().getByID(11));
        filter.setEntryType(EntryFilter.EntryType.NOTE);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(1, entryList.size());
        assertEquals(1, entryList.getAt(0).getId());
    }

    /**
     * Tests for getEntriesForFilter() - scenario: the filter contains the criteria for sport type 2.
     * No notes need to be found for this filter.
     */
    @Test
    public void testGetEntriesForFilter15() {

        EntryFilter filter = new EntryFilter();
        filter.setDateStart(LocalDate.of(2003, 1, 1));
        filter.setDateEnd(LocalDate.of(2003, 12, 31));
        filter.setSportType(sportTypeList.getByID(2));
        filter.setEntryType(EntryFilter.EntryType.NOTE);

        EntryList<Note> entryList = list.getEntriesForFilter(filter);
        assertEquals(0, entryList.size());
    }

}
