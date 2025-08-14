package com.wornux.data.enums;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

@Getter
public enum PetType {
  PERRO(
      Arrays.asList(
          "Labrador Retriever",
          "Golden Retriever",
          "Pastor Alemán",
          "Bulldog Francés",
          "Bulldog",
          "Poodle",
          "Beagle",
          "Rottweiler",
          "Yorkshire Terrier",
          "Dachshund",
          "Husky Siberiano",
          "Bóxer",
          "Border Collie",
          "Pastor Australiano",
          "Shih Tzu")),

  GATO(
      Arrays.asList(
          "Persa",
          "Maine Coon",
          "Británico de Pelo Corto",
          "Ragdoll",
          "Abisinio",
          "Siamés",
          "Scottish Fold",
          "Sphynx",
          "Bengala",
          "Azul Ruso",
          "Americano de Pelo Corto",
          "Birmano",
          "Oriental de Pelo Corto",
          "Devon Rex",
          "Bosque de Noruega")),

  AVE(
      Arrays.asList(
          "Periquito",
          "Cacatúa Ninfa",
          "Canario",
          "Agapornis",
          "Jilguero",
          "Conuro",
          "Guacamaya",
          "Loro Gris Africano",
          "Cacatúa",
          "Periquito")),

  CONEJO(
      Arrays.asList(
          "Holland Lop",
          "Enano de los Países Bajos",
          "Mini Rex",
          "Lionhead",
          "Gigante de Flandes",
          "Angora",
          "Holandés",
          "Mini Lop",
          "Rex",
          "Himalaya")),

  HAMSTER(
      Arrays.asList(
          "Sirio", "Campbell Enano Ruso", "Winter White Enano Ruso", "Roborovski", "Chino")),

  REPTIL(
      Arrays.asList(
          "Dragón Barbado",
          "Gecko Leopardo",
          "Python Bola",
          "Culebra del Maíz",
          "Escinco Lengua Azul",
          "Gecko Crestado",
          "Galápago de Orejas Rojas",
          "Iguana Verde",
          "Camaleón",
          "Lagarto Monitor")),

  OTRO(Arrays.asList("Mixto", "Desconocido", "Otro"));

  private final List<String> breeds;

  PetType(List<String> breeds) {
    this.breeds = breeds;
  }

  public boolean isValidBreedForType(String breed) {
    return breeds.contains(breed);
  }
}
