package app.utils;

import app.enums.Ability;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ContentHashingTest
{
    @Nested
    @DisplayName("Sha256Hex")
    class Sha256Hex
    {
        @Test
        @DisplayName("Should return same hash for same input")
        void deterministic()
        {
            String result1 = ContentHashing.sha256Hex("hello");
            String result2 = ContentHashing.sha256Hex("hello");

            assertThat(result1, equalTo(result2));
        }

        @Test
        @DisplayName("Should return different hash for different input")
        void differentInputs()
        {
            String result1 = ContentHashing.sha256Hex("hello");
            String result2 = ContentHashing.sha256Hex("world");

            assertThat(result1, not(equalTo(result2)));
        }
    }

    @Nested
    @DisplayName("Normalize")
    class Normalize
    {
        @Test
        @DisplayName("Should return empty string for null")
        void nullReturnsEmpty()
        {
            assertThat(ContentHashing.normalize(null), equalTo(""));
        }

        @Test
        @DisplayName("Should trim whitespace")
        void trimsWhitespace()
        {
            assertThat(ContentHashing.normalize("  hello  "), equalTo("hello"));
        }

        @Test
        @DisplayName("Should return unchanged clean string")
        void cleanStringUnchanged()
        {
            assertThat(ContentHashing.normalize("hello"), equalTo("hello"));
        }
    }

    @Nested
    @DisplayName("NormalizeLower")
    class NormalizeLower
    {
        @Test
        @DisplayName("Should return empty string for null")
        void nullReturnsEmpty()
        {
            assertThat(ContentHashing.normalizeLower(null), equalTo(""));
        }

        @Test
        @DisplayName("Should trim and lowercase")
        void trimsAndLowers()
        {
            assertThat(ContentHashing.normalizeLower("  HELLO  "), equalTo("hello"));
        }

        @Test
        @DisplayName("Should return unchanged already lowercase string")
        void alreadyLowercaseUnchanged()
        {
            assertThat(ContentHashing.normalizeLower("hello"), equalTo("hello"));
        }
    }

    @Nested
    @DisplayName("JoinSorted")
    class JoinSorted
    {
        @Test
        @DisplayName("Should return empty string for null")
        void nullReturnsEmpty()
        {
            assertThat(ContentHashing.joinSorted(null), equalTo(""));
        }

        @Test
        @DisplayName("Should return empty string for empty collection")
        void emptyReturnsEmpty()
        {
            assertThat(ContentHashing.joinSorted(List.of()), equalTo(""));
        }

        @Test
        @DisplayName("Should join values sorted case-insensitively")
        void sortsCaseInsensitive()
        {
            assertThat(ContentHashing.joinSorted(List.of("Zebra", "apple", "Mango")), equalTo("apple,Mango,Zebra"));
        }

        @Test
        @DisplayName("Should filter null values from collection")
        void filtersNulls()
        {
            List<String> values = new java.util.ArrayList<>();

            values.add("banana");
            values.add(null);
            values.add("apple");

            assertThat(ContentHashing.joinSorted(values), equalTo("apple,banana"));
        }

        @Test
        @DisplayName("Should produce same output regardless of input order")
        void orderIndependent()
        {
            String result1 = ContentHashing.joinSorted(List.of("Zebra", "apple", "Mango"));
            String result2 = ContentHashing.joinSorted(List.of("Mango", "Zebra", "apple"));

            assertThat(result1, equalTo(result2));
        }
    }

    @Nested
    @DisplayName("JoinSortedMapped")
    class JoinSortedMapped
    {
        @Test
        @DisplayName("Should return empty string for null")
        void nullReturnsEmpty()
        {
            assertThat(ContentHashing.<String>joinSortedMapped(null, value -> value), equalTo(""));
        }

        @Test
        @DisplayName("Should apply mapper before sorting and joining")
        void appliesMapper()
        {
            List<String> values = List.of("Zebra", "apple", "Mango");

            assertThat(ContentHashing.joinSortedMapped(values, String::toLowerCase), equalTo("apple,mango,zebra"));
        }

        @Test
        @DisplayName("Should produce same output regardless of input order")
        void orderIndependent()
        {
            List<String> values1 = List.of("Zebra", "apple", "Mango");
            List<String> values2 = List.of("Mango", "Zebra", "apple");

            assertThat(
                    ContentHashing.joinSortedMapped(values1, String::toLowerCase),
                    equalTo(ContentHashing.joinSortedMapped(values2, String::toLowerCase))
            );
        }
    }

    @Nested
    @DisplayName("JoinSortedEnumMap")
    class JoinSortedEnumMap
    {
        @Test
        @DisplayName("Should return empty string for null")
        void nullReturnsEmpty()
        {
            assertThat(ContentHashing.joinSortedEnumMap(null), equalTo(""));
        }

        @Test
        @DisplayName("Should return empty string for empty map")
        void emptyReturnsEmpty()
        {
            assertThat(ContentHashing.<Ability>joinSortedEnumMap(Map.of()), equalTo(""));
        }

        @Test
        @DisplayName("Should format entries as NAME:value")
        void formatsEntries()
        {
            assertThat(ContentHashing.joinSortedEnumMap(Map.of(Ability.STRENGTH, 2)), equalTo("STRENGTH:2"));
        }

        @Test
        @DisplayName("Should sort entries by enum name")
        void sortsByEnumName()
        {
            Map<Ability, Integer> map = Map.of(Ability.STRENGTH, 2, Ability.DEXTERITY, 1, Ability.CONSTITUTION, 3);

            assertThat(ContentHashing.joinSortedEnumMap(map), equalTo("CONSTITUTION:3,DEXTERITY:1,STRENGTH:2"));
        }

        @Test
        @DisplayName("Should produce same output regardless of map insertion order")
        void orderIndependent()
        {
            Map<Ability, Integer> map1 = Map.of(Ability.STRENGTH, 2, Ability.DEXTERITY, 1);
            Map<Ability, Integer> map2 = Map.of(Ability.DEXTERITY, 1, Ability.STRENGTH, 2);

            assertThat(ContentHashing.joinSortedEnumMap(map1), equalTo(ContentHashing.joinSortedEnumMap(map2)));
        }
    }
}