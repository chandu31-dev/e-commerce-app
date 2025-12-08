package com.catchy.payout;

import com.catchy.model.Payout;

public interface PayoutProvider {
    /**
     * Attempt to pay a payout. Returns a non-null reference string when successful,
     * or throws an exception on failure.
     */
    String pay(Payout payout) throws Exception;
}
