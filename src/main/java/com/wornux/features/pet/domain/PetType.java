package com.wornux.features.pet.domain;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum PetType {
    DOG(Arrays.asList("Labrador Retriever", "Golden Retriever", "German Shepherd", "French Bulldog", "Bulldog",
            "Poodle", "Beagle", "Rottweiler", "Yorkshire Terrier", "Dachshund", "Siberian Husky", "Boxer",
            "Border Collie", "Australian Shepherd", "Shih Tzu")),

    CAT(Arrays.asList("Persian", "Maine Coon", "British Shorthair", "Ragdoll", "Abyssinian", "Siamese", "Scottish Fold",
            "Sphynx", "Bengal", "Russian Blue", "American Shorthair", "Birman", "Oriental Shorthair", "Devon Rex",
            "Norwegian Forest Cat")),

    BIRD(Arrays.asList("Parakeet", "Cockatiel", "Canary", "Lovebird", "Finch", "Conure", "Macaw", "African Grey",
            "Cockatoo", "Budgerigar")),

    RABBIT(Arrays.asList("Holland Lop", "Netherland Dwarf", "Mini Rex", "Lionhead", "Flemish Giant", "Angora", "Dutch",
            "Mini Lop", "Rex", "Himalayan")),

    HAMSTER(Arrays.asList("Syrian", "Dwarf Campbell Russian", "Dwarf Winter White Russian", "Roborovski", "Chinese")),

    REPTILE(Arrays.asList("Bearded Dragon", "Leopard Gecko", "Ball Python", "Corn Snake", "Blue-Tongued Skink",
            "Crested Gecko", "Red-Eared Slider", "Green Iguana", "Chameleon", "Monitor Lizard")),

    OTHER(Arrays.asList("Mixed", "Unknown", "Other"));

    private final List<String> breeds;

    PetType(List<String> breeds) {
        this.breeds = breeds;
    }

    public boolean isValidBreedForType(String breed) {
        return breeds.contains(breed);
    }
}
