package com.zoolandia.app.features.pet.domain;

public enum PetType {
    DOG(
            "Perro",
            new String[] {
                    "Labrador Retriever", "Golden Retriever", "German Shepherd", "Pug", "French Bulldog", "Bulldog",
                    "Poodle", "Beagle", "Rottweiler", "Yorkshire Terrier", "Dachshund", "Siberian Husky",
                    "Boxer", "Chihuahua", "Shih Tzu", "Boston Terrier", "Cocker Spaniel", "Border Collie",
                    "Australian Shepherd", "Great Dane", "Doberman Pinscher", "Mastiff", "Saint Bernard", "Dalmatian",
                    "Pitbull Terrier", "Otro"
            }
    ),
    CAT(
            "Gato",
            new String[] {
                    "Persian", "Siamese", "Maine Coon", "Sphynx", "Bengal", "British Shorthair", "Ragdoll",
                    "Abyssinian", "Russian Blue", "Scottish Fold", "Sphynx", "American Shorthair", "Exotic Shorthair",
                    "Devon Rex", "Norwegian Forest", "Birman", "Oriental Shorthair", "Manx", "Turkish Angora", "Bombay",
                    "Munchkin", "Otro"
            }
    ),
    BIRD(
            "Ave",
            new String[] {
                    "Canary", "Parakeet", "Cockatiel", "Lovebird", "Finch", "Conure", "Macaw", "African Grey",
                    "Budgerigar", "Cockatoo", "Cardinal", "Parrotlet", "Caique", "Amazon Parrot", "Eclectus", "Otro"
            }
    ),
    // ...otros tipos...
    UNKNOWN("Desconocido", new String[] { "Desconocido" });

    private final String displayName;
    private final String[] breeds;

    PetType(String displayName, String[] breeds) {
        this.displayName = displayName;
        this.breeds = breeds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getBreeds() {
        return breeds;
    }
}