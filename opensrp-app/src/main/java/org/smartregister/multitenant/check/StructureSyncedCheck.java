package org.smartregister.multitenant.check;

import android.support.annotation.NonNull;

import org.smartregister.domain.FetchStatus;
import org.smartregister.multitenant.PreResetAppCheck;
import org.smartregister.multitenant.exception.PreResetAppOperationException;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.StructureRepository;
import org.smartregister.view.activity.DrishtiApplication;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - nek.eam@gmail.com on 16-04-2020.
 */
public class StructureSyncedCheck implements PreResetAppCheck, SyncStatusBroadcastReceiver.SyncStatusListener {

    public static final String UNIQUE_NAME = "StructureSyncedCheck";

    @Override
    public boolean isCheckOk(@NonNull DrishtiApplication drishtiApplication) {
        return isStructuresSynced(drishtiApplication);
    }

    @Override
    public void performPreResetAppOperations(@NonNull DrishtiApplication application) throws PreResetAppOperationException {
        StructuresSync structuresSync = new StructuresSync(application);
        structuresSync.performSync();
    }

    public boolean isStructuresSynced(@NonNull DrishtiApplication application) {
        StructureRepository structureRepository = application.getContext().getStructureRepository();
        if (structureRepository != null) {
            return structureRepository.getUnsyncedStructuresCount() == 0;
        }

        return false;
    }


    @Override
    public void onSyncStart() {
        // Do nothing for now
        Timber.e("Sync is starting");
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        if (fetchStatus == FetchStatus.fetchProgress) {
            Timber.e("Sync progress is %s", fetchStatus.displayValue());
        }
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        // Do nothing for now
        Timber.e("The sync is complete");
    }

    @NonNull
    @Override
    public String getUniqueName() {
        return UNIQUE_NAME;
    }
}
