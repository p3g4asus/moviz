package com.moviz.workers;

import android.accounts.Account;

import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sync status observer that reports back via a callback interface when syncing has begun
 * and finished.
 */
public class MySyncStatusObserver implements SyncStatusObserver {
    /**
     * Defines the various sync states for an account.
     */
    private enum SyncState {
        /**
         * Indicates a sync is pending.
         */
        PENDING,
        /**
         * Indicates a sync is no longer pending but isn't active yet.
         */
        PENDING_ACTIVE,
        /**
         * Indicates a sync is active.
         */
        ACTIVE,
        /**
         * Indicates syncing is finished.
         */
        FINISHED
    }

    /**
     * Lifecycle events.
     */
    public interface Callback {
        /**
         * Indicates syncing of calendars has begun.
         */
        void onSyncsStarted();

        /**
         * Indicates syncing of calendars has finished.
         */
        void onSyncsFinished();
    }

    /**
     * The original list of accounts that are being synced.
     */
    @NonNull private final List<Account> mAccounts;
    /**
     * Map of accounts and their current sync states.
     */
    private final Map<Account, SyncState> mAccountSyncState =
            Collections.synchronizedMap(new HashMap<Account, SyncState>());

    /**
     * The calendar authority we're listening for syncs on.
     */
    @NonNull private final String mCalendarAuthority;
    /**
     * Callback implementation.
     */
    @Nullable private final Callback mCallback;

    /**
     * {@code true} when a "sync started" callback has been called.
     *
     * <p>Keeps us from reporting this event more than once.</p>
     */
    private boolean mSyncStartedReported;
    /**
     * Provider handle returned from
     * {@link ContentResolver#addStatusChangeListener(int, SyncStatusObserver)} used to
     * unregister for sync status changes.
     */
    @Nullable private Object mProviderHandle;

    /**
     * Default constructor.
     *
     * @param accounts the accounts to monitor syncing for
     * @param calendarAuthority the calendar authority for the syncs
     * @param callback optional callback interface to receive events
     */
    public MySyncStatusObserver(@NonNull final Account[] accounts,
                                @NonNull final String calendarAuthority, @Nullable final Callback callback) {
        mAccounts = Arrays.asList(accounts);

        mCalendarAuthority = calendarAuthority;
        mCallback = callback;
    }

    /**
     * Sets the provider handle to unregister for sync status changes with.
     */
    public void setProviderHandle(@Nullable final Object providerHandle) {
        mProviderHandle = providerHandle;
    }

    @Override
    public void onStatusChanged(int which) {
        for (final Account account : mAccounts) {
            if (which == ContentResolver.SYNC_OBSERVER_TYPE_PENDING) {
                if (ContentResolver.isSyncPending(account, mCalendarAuthority)) {
                    // There is now a pending sync.
                    mAccountSyncState.put(account, SyncState.PENDING);
                } else {
                    // There is no longer a pending sync.
                    mAccountSyncState.put(account, SyncState.PENDING_ACTIVE);
                }
            } else if (which == ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                if (ContentResolver.isSyncActive(account, mCalendarAuthority)) {
                    // There is now an active sync.
                    mAccountSyncState.put(account, SyncState.ACTIVE);

                    if (!mSyncStartedReported && mCallback != null) {
                        mCallback.onSyncsStarted();
                        mSyncStartedReported = true;
                    }
                } else {
                    // There is no longer an active sync.
                    mAccountSyncState.put(account, SyncState.FINISHED);
                }
            }
        }

        // We haven't finished processing sync states for all accounts yet
        if (mAccounts.size() != mAccountSyncState.size()) return;

        // Check if any accounts are not finished syncing yet. If so bail
        for (final SyncState syncState : mAccountSyncState.values()) {
            if (syncState != SyncState.FINISHED) return;
        }

        // 1. Unregister for sync status changes
        if (mProviderHandle != null) {
            ContentResolver.removeStatusChangeListener(mProviderHandle);
        }

        // 2. Report back that all syncs are finished
        if (mCallback != null) {
            mCallback.onSyncsFinished();
        }
    }
}
