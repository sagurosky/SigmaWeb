/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mantenimiento.gestorTareas.util;

import java.text.Normalizer;
import java.time.LocalDateTime;

/**
 *
 * @author daniel
 */
public class Convertidor {
    
    
    
    public static String aCamelCase(String text){
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 1️⃣ Eliminar acentos
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        // 2️⃣ Eliminar caracteres especiales, excepto números y espacios
        normalizedText = normalizedText.replaceAll("[^a-zA-Z0-9\\s]", "");

        // 3️⃣ Convertir a camel case
        StringBuilder camelCaseString = new StringBuilder();
        boolean nextCharUpperCase = false;

        for (char currentChar : normalizedText.toCharArray()) {
            if (Character.isWhitespace(currentChar)) {
                nextCharUpperCase = true;
            } else {
                camelCaseString.append(nextCharUpperCase ? Character.toUpperCase(currentChar)
                        : Character.toLowerCase(currentChar));
                nextCharUpperCase = false;
            }
        }

        return camelCaseString.toString();

    }
    
    
    
    public static String deCamelCase(String camelCase) {
        // Convertir el primer carácter a mayúscula

//si necesito que el primer caracter sea mayuscula reemplazar por la siguiente linea
//        StringBuilder result = new StringBuilder(camelCase.substring(0, 1).toUpperCase());
        StringBuilder result = new StringBuilder(camelCase.substring(0, 1));

        // Recorrer el resto de la cadena
        for (int i = 1; i < camelCase.length(); i++) {
            char currentChar = camelCase.charAt(i);

            // Si el carácter actual es mayúscula, añadir un espacio y convertirlo a minúscula
            if (Character.isUpperCase(currentChar)||Character.isDigit(currentChar)) {
                result.append(" ");
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }
    
    
    
    
    
}
