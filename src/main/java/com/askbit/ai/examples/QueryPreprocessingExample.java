package com.askbit.ai.examples;

import com.askbit.ai.service.QueryPreprocessingService;

/**
 * Example demonstrating the Query Preprocessing Service capabilities
 */
public class QueryPreprocessingExample {

    public static void main(String[] args) {
        QueryPreprocessingService service = new QueryPreprocessingService();

        System.out.println("=== Query Preprocessing Service Demo ===\n");

        // Example 1: Spelling correction
        System.out.println("1. Spelling Correction:");
        demonstratePreprocessing(service, "What is the increament polcy for employe?");

        // Example 2: Abbreviation expansion
        System.out.println("\n2. Abbreviation Expansion:");
        demonstratePreprocessing(service, "What is pvp and wfh policy?");

        // Example 3: Special characters
        System.out.println("\n3. Special Character Handling:");
        demonstratePreprocessing(service, "What is the sallary??? @#$");

        // Example 4: Text speak
        System.out.println("\n4. Text Speak Conversion:");
        demonstratePreprocessing(service, "pls tell me wat is the leave policy");

        // Example 5: Combined issues
        System.out.println("\n5. Multiple Issues Combined:");
        demonstratePreprocessing(service, "wat is hr polcy for employe benifits???");

        // Example 6: Abbreviations
        System.out.println("\n6. Multiple Abbreviations:");
        demonstratePreprocessing(service, "Tell me about ctc, pf and esi");

        // Example 7: Question mark addition
        System.out.println("\n7. Question Mark Addition:");
        demonstratePreprocessing(service, "What is the leave policy");

        // Example 8: Whitespace normalization
        System.out.println("\n8. Whitespace Normalization:");
        demonstratePreprocessing(service, "What    is     the    policy");

        // Example 9: Informal phrases
        System.out.println("\n9. Informal Phrase Conversion:");
        demonstratePreprocessing(service, "can u tell me about flexnext");

        // Example 10: Complex real-world example
        System.out.println("\n10. Complex Real-World Example:");
        demonstratePreprocessing(service, "pls tell me wat is the eligable criteria for pvp increament in fy 2024???");
    }

    private static void demonstratePreprocessing(QueryPreprocessingService service, String original) {
        String processed = service.preprocessQuestion(original);
        System.out.println("  Original:   \"" + original + "\"");
        System.out.println("  Processed:  \"" + processed + "\"");

        if (original.equals(processed)) {
            System.out.println("  (No changes needed)");
        } else {
            System.out.println("  ✓ Improved!");
        }
    }
}

