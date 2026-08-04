package com.ansh.smart_commerce.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntentClassifierTest {

    @Test
    void classify_shouldReturnGeneralForBlankMessage() {
        assertEquals(IntentClassifier.Intent.GENERAL, IntentClassifier.classify(null));
        assertEquals(IntentClassifier.Intent.GENERAL, IntentClassifier.classify("   "));
    }

    @Test
    void classify_shouldDetectCommonIntents() {
        assertEquals(IntentClassifier.Intent.PRODUCT_COMPARISON, IntentClassifier.classify("compare iphone vs samsung"));
        assertEquals(IntentClassifier.Intent.RECOMMENDATION, IntentClassifier.classify("what accessories go with my laptop"));
        assertEquals(IntentClassifier.Intent.ORDER_SUPPORT, IntentClassifier.classify("where is my order"));
        assertEquals(IntentClassifier.Intent.COUPON_QUERY, IntentClassifier.classify("any coupon available?"));
        assertEquals(IntentClassifier.Intent.PRODUCT_SEARCH, IntentClassifier.classify("need a gaming laptop under 90000"));
        assertEquals(IntentClassifier.Intent.GENERAL_FAQ, IntentClassifier.classify("how does shipping work"));
    }
}