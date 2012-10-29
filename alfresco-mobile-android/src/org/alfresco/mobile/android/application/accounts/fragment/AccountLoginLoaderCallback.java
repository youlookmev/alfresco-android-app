/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.application.accounts.fragment;

import org.alfresco.mobile.android.api.asynchronous.CloudSessionLoader;
import org.alfresco.mobile.android.api.asynchronous.LoaderResult;
import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.exceptions.AlfrescoSessionException;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.authentication.OAuthData;
import org.alfresco.mobile.android.application.MainActivity;
import org.alfresco.mobile.android.application.R;
import org.alfresco.mobile.android.application.accounts.Account;
import org.alfresco.mobile.android.application.accounts.AccountDAO;
import org.alfresco.mobile.android.application.intent.IntentIntegrator;
import org.alfresco.mobile.android.application.manager.ActionManager;
import org.alfresco.mobile.android.application.utils.SessionUtils;
import org.alfresco.mobile.android.ui.manager.MessengerManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;

@TargetApi(11)
public class AccountLoginLoaderCallback extends AbstractSessionCallback
{

    private static final String TAG = "AccountLoginLoaderCallback";

    private Account acc;

    private ProgressDialog mProgressDialog;

    public AccountLoginLoaderCallback(Activity activity, Account acc)
    {
        this.activity = activity;
        this.acc = acc;
    }

    public AccountLoginLoaderCallback(Activity activity, Account acc, OAuthData data)
    {
        this.activity = activity;
        this.acc = acc;
        this.data = data;
    }

    @Override
    public Loader<LoaderResult<AlfrescoSession>> onCreateLoader(final int id, Bundle args)
    {

        Loader<LoaderResult<AlfrescoSession>> loader = null;
        if (data != null)
        {
            mProgressDialog = ProgressDialog.show(activity, getText(R.string.wait_title),
                    getText(R.string.wait_message), true, true, new OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialog)
                        {
                            activity.getLoaderManager().destroyLoader(id);
                        }
                    });
            loader = getSessionLoader(new AccountSettingsHelper(activity, acc, data));
        }
        else
        {
            loader = getSessionLoader(new AccountSettingsHelper(activity, acc));
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<AlfrescoSession>> loader, LoaderResult<AlfrescoSession> results)
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
        if (!results.hasException())
        {
            saveNewOauthData(loader);

            activity.getLoaderManager().destroyLoader(loader.getId());
            SessionUtils.setsession(activity, results.getData());
            Intent i = new Intent(activity, MainActivity.class);
            i.setAction(IntentIntegrator.ACTION_LOAD_SESSION_FINISH);
            activity.startActivity(i);
        }
        else
        {
            switch ((int) acc.getTypeId())
            {
                case Account.TYPE_ALFRESCO_TEST_OAUTH:
                case Account.TYPE_ALFRESCO_CLOUD:

                    // FIXME Wrong SDK Error
                    // Manage Cloud Session Error
                    if (results.getException() instanceof AlfrescoSessionException)
                    {
                        // Case CmisConnexionException ==> Token expired
                        AlfrescoSessionException ex = ((AlfrescoSessionException) results.getException());
                        if (ex.getMessage().contains("No authentication challenges found") || ex.getErrorCode() == 100)
                        {
                            manageException(results);
                        }
                    }

                    if (results.getException() instanceof AlfrescoServiceException)
                    {
                        AlfrescoServiceException ex = ((AlfrescoServiceException) results.getException());
                        if (ex.getErrorCode() == 104)
                        {
                            manageException(results);
                        }
                    }
                    break;
                default:
                    MessengerManager.showLongToast(activity, getText(R.string.error_session_creation)
                            + results.getException().getMessage());
                    break;
            }
            Log.e(TAG, Log.getStackTraceString(results.getException()));
        }
        activity.setProgressBarIndeterminateVisibility(false);
    }

    private void manageException(LoaderResult<AlfrescoSession> results)
    {
        MessengerManager.showLongToast(activity, getText(R.string.error_session_expired));
        ActionManager.actionRequestUserAuthentication(activity, acc);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<AlfrescoSession>> loader)
    {
        saveNewOauthData(loader);

        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
        }
    }

    private void saveNewOauthData(Loader<LoaderResult<AlfrescoSession>> loader)
    {
        Log.d(TAG, loader.toString());
        switch ((int) acc.getTypeId())
        {
            case Account.TYPE_ALFRESCO_TEST_OAUTH:
            case Account.TYPE_ALFRESCO_CLOUD:
                AccountDAO accountDao = new AccountDAO(activity, SessionUtils.getDataBaseManager(activity).getWriteDb());
                if (accountDao.update(acc.getId(), acc.getDescription(), acc.getUrl(), acc.getUsername(), acc
                        .getPassword(), acc.getRepositoryId(), Integer.valueOf((int) acc.getTypeId()), null,
                        ((CloudSessionLoader) loader).getOAuthData().getAccessToken(), ((CloudSessionLoader) loader)
                                .getOAuthData().getRefreshToken()))
                {
                    SessionUtils.setAccount(activity, accountDao.findById(acc.getId()));
                }
                else
                {
                    MessengerManager.showLongToast(activity, "Error during token update");
                }
                break;
        }
    }

}
