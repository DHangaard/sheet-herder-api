package app.testutils;

import app.enums.Ability;
import app.enums.LanguageType;
import app.enums.Size;
import app.persistence.daos.reference.implementations.*;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.reference.*;
import app.utils.ContentHashing;
import jakarta.persistence.EntityManagerFactory;

import java.util.*;

public class ReferenceDataPopulator
{
    private final IReferenceDAO<Trait> traitDAO;
    private final IReferenceDAO<Language> languageDAO;
    private final IReferenceDAO<Race> raceDAO;
    private final IReferenceDAO<Subrace> subraceDAO;
    private final Map<String, IEntity> seeded;

    public ReferenceDataPopulator(EntityManagerFactory emf)
    {
        this.traitDAO = new TraitDAO(emf);
        this.languageDAO = new LanguageDAO(emf);
        this.raceDAO = new RaceDAO(emf);
        this.subraceDAO = new SubraceDAO(emf);
        this.seeded = new HashMap<>();
    }

    public void populate()
    {
        populateTraits();
        populateLanguages();
        populateRaces();
        populateSubraces();
    }

    public Map<String, IEntity> getSeededData()
    {
        return Collections.unmodifiableMap(seeded);
    }

    private void populateTraits()
    {
        Trait darkvision = traitDAO.create(new Trait(
                "Darkvision",
                List.of("You have superior vision in dark and dim conditions. You can see in dim light within 60 feet of you as if it were bright light, and in darkness as if it were dim light. You cannot discern color in darkness, only shades of gray."),
                ContentHashing.sha256Hex("Darkvision")
        ));
        Trait keenSenses = traitDAO.create(new Trait(
                "Keen Senses",
                List.of("You have proficiency in the Perception skill."),
                ContentHashing.sha256Hex("Keen Senses")
        ));
        Trait feyAncestry = traitDAO.create(new Trait(
                "Fey Ancestry",
                List.of("You have advantage on saving throws against being charmed, and magic cannot put you to sleep."),
                ContentHashing.sha256Hex("Fey Ancestry")
        ));
        Trait trance = traitDAO.create(new Trait(
                "Trance",
                List.of("Elves do not need to sleep. Instead, they meditate deeply, remaining semiconscious, for 4 hours a day."),
                ContentHashing.sha256Hex("Trance")
        ));
        Trait brave = traitDAO.create(new Trait(
                "Brave",
                List.of("You have advantage on saving throw against being frightened."),
                ContentHashing.sha256Hex("Brave")
        ));

        seeded.put("darkvision", darkvision);
        seeded.put("keenSenses", keenSenses);
        seeded.put("feyAncestry", feyAncestry);
        seeded.put("trance", trance);
        seeded.put("brave", brave);
    }

    private void populateLanguages()
    {
        Language elvish = languageDAO.create(new Language(
                "Elvish",
                "Elvish is fluid, with subtle intonations and intricate grammar.",
                LanguageType.STANDARD,
                List.of("Elves"),
                "Elvish",
                ContentHashing.sha256Hex("Elvish")
        ));
        Language common = languageDAO.create(new Language(
                "Common",
                null,
                LanguageType.STANDARD,
                List.of("Humans"),
                "Common",
                ContentHashing.sha256Hex("Common")
        ));
        Language sylvan = languageDAO.create(new Language(
                "Sylvan",
                null,
                LanguageType.EXOTIC,
                List.of("Fey creatures"),
                "Elvish",
                ContentHashing.sha256Hex("Sylvan")
        ));

        seeded.put("elvish", elvish);
        seeded.put("common", common);
        seeded.put("sylvan", sylvan);
    }

    private void populateRaces()
    {
        Trait darkvision = (Trait) seeded.get("darkvision");
        Trait keenSenses = (Trait) seeded.get("keenSenses");
        Trait feyAncestry = (Trait) seeded.get("feyAncestry");
        Trait trance = (Trait) seeded.get("trance");
        Language elvish = (Language) seeded.get("elvish");
        Language common = (Language) seeded.get("common");

        Race elf = raceDAO.create(new Race(
                "Elf",
                30,
                Map.of(Ability.DEXTERITY, 2),
                "Although elves reach physical maturity at about the same age as humans...",
                "Elves love freedom, variety, and self-expression...",
                Size.MEDIUM,
                "Elves range from under 5 to over 6 feet tall and have slender builds. Your size is Medium.",
                Set.of(elvish, common),
                "You can speak, read, and write Common and Elvish.",
                Set.of(darkvision, keenSenses, feyAncestry, trance),
                ContentHashing.sha256Hex("Elf")
        ));
        Race human = raceDAO.create(new Race(
                "Human",
                30,
                Map.of(Ability.STRENGTH, 1, Ability.CONSTITUTION, 1, Ability.DEXTERITY, 1,
                        Ability.INTELLIGENCE, 1, Ability.WISDOM, 1, Ability.CHARISMA, 1),
                "Humans reach adulthood in their late teens and live less than a century.",
                "Humans tend toward no particular alignment. The best and the worst are found among them.",
                Size.MEDIUM,
                "Humans vary widely in height and build. Your size is Medium.",
                Set.of(common),
                "You can speak, read, and write Common and one extra language of your choice.",
                Set.of(),
                ContentHashing.sha256Hex("Human")
        ));

        seeded.put("elf", elf);
        seeded.put("human", human);
    }

    private void populateSubraces()
    {
        Race elf = (Race) seeded.get("elf");
        Trait keenSenses = (Trait) seeded.get("keenSenses");
        Trait brave = (Trait) seeded.get("brave");

        Subrace highElf = subraceDAO.create(new Subrace(
                "High Elf",
                "As a high elf, you have a keen mind and a mastery of at least the basics of magic.",
                elf,
                Map.of(Ability.INTELLIGENCE, 1),
                Set.of(keenSenses),
                ContentHashing.sha256Hex("High Elf")
        ));

        Subrace woodElf = subraceDAO.create(new Subrace(
                "Wood Elf",
                "As a wood elf, you have keen senses and intuition, and your fleet feet carry you quickly through your native forests.",
                elf,
                Map.of(Ability.WISDOM, 1),
                Set.of(brave),
                ContentHashing.sha256Hex("Wood Elf")
        ));

        seeded.put("highElf", highElf);
        seeded.put("woodElf", woodElf);
    }
}