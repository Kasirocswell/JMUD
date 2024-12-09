package com.mudgame.entities;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum ClassSpecialization {
    // Soldier Specializations
    BOUNTY_HUNTER("Bounty Hunter", CharacterClass.SOLDIER, true,
            "A licensed hunter who tracks down criminals and fugitives for legal authorities.",
            new String[]{"Target Tracking", "Non-lethal Takedowns", "Mark Target", "Capture Protocols"}),
    MERCENARY("Mercenary", CharacterClass.SOLDIER, false,
            "A skilled warrior who sells their combat expertise to the highest bidder.",
            new String[]{"Enhanced Combat Stims", "Black Market Weapons", "Explosive Specialization", "Terror Tactics"}),

    // Pilot Specializations
    COMMERCIAL_PILOT("Commercial Pilot", CharacterClass.PILOT, true,
            "A licensed pilot who operates legitimate cargo and passenger vessels.",
            new String[]{"Efficient Flight Paths", "Emergency Protocols", "Passenger Safety", "Fleet Management"}),
    SMUGGLER("Smuggler", CharacterClass.PILOT, false,
            "A daring pilot who specializes in moving illegal cargo past authorities.",
            new String[]{"Stealth Flight", "Sensor Jamming", "Hidden Compartments", "Quick Escape"}),

    // Hacker Specializations
    SECURITY_EXPERT("Security Expert", CharacterClass.HACKER, true,
            "A cybersecurity specialist who protects networks and data from threats.",
            new String[]{"Firewall Enhancement", "Threat Detection", "System Hardening", "White Hat Techniques"}),
    CYBER_CRIMINAL("Cyber Criminal", CharacterClass.HACKER, false,
            "A skilled hacker who exploits system vulnerabilities for personal gain.",
            new String[]{"System Infiltration", "Data Theft", "Account Hijacking", "Black Market Access"}),

    // Engineer Specializations
    SYSTEMS_ENGINEER("Systems Engineer", CharacterClass.ENGINEER, true,
            "A certified engineer who maintains and improves legal technology systems.",
            new String[]{"Safety Protocols", "System Optimization", "Power Management", "Legal Modifications"}),
    TECH_SCAVENGER("Tech Scavenger", CharacterClass.ENGINEER, false,
            "An engineer who salvages and modifies technology through any means necessary.",
            new String[]{"Illegal Mods", "Weapon Modifications", "Scrap Recovery", "Experimental Tech"}),

    // Medic Specializations
    LICENSED_PHYSICIAN("Licensed Physician", CharacterClass.MEDIC, true,
            "A certified medical professional who operates within galactic healthcare laws.",
            new String[]{"Advanced First Aid", "Legal Stim Usage", "Emergency Response", "Hospital Management"}),
    BACK_ALLEY_DOC("Back Alley Doctor", CharacterClass.MEDIC, false,
            "An unlicensed medic who provides illegal medical services.",
            new String[]{"Illegal Cybernetics", "Black Market Stims", "Underground Clinics", "Memory Modification"});

    private final String name;
    private final CharacterClass baseClass;
    private final boolean lawful;
    private final String description;
    private final String[] abilities;

    ClassSpecialization(String name, CharacterClass baseClass, boolean lawful, String description, String[] abilities) {
        this.name = name;
        this.baseClass = baseClass;
        this.lawful = lawful;
        this.description = description;
        this.abilities = abilities;
    }

    public String getName() { return name; }
    public CharacterClass getBaseClass() { return baseClass; }
    public boolean isLawful() { return lawful; }
    public String getDescription() { return description; }
    public String[] getAbilities() { return abilities.clone(); }

    public static List<ClassSpecialization> getSpecializationsForClass(CharacterClass baseClass) {
        return Arrays.stream(values())
                .filter(spec -> spec.getBaseClass() == baseClass)
                .collect(Collectors.toList());
    }

    public static Optional<ClassSpecialization> fromName(String name) {
        return Arrays.stream(values())
                .filter(spec -> spec.getName().equalsIgnoreCase(name))
                .findFirst();
    }
}