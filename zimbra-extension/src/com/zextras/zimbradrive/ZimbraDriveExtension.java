/*
 * Copyright (C) 2017 ZeXtras SRL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.zextras.zimbradrive;

import com.zextras.zimbradrive.soap.NcSoapService;
import com.zextras.zimbradrive.statustest.ConnectionTestUtils;
import org.openzal.zal.extension.ZalExtension;
import org.openzal.zal.extension.ZalExtensionController;
import org.openzal.zal.extension.Zimbra;
import org.openzal.zal.http.HttpServiceManager;
import org.openzal.zal.log.ZimbraLog;
import org.openzal.zal.soap.SoapServiceManager;

import java.lang.ref.WeakReference;


public class ZimbraDriveExtension implements ZalExtension
{
  public static final String SOAP_NAMESPACE = "urn:zimbraDrive";

  private final HttpServiceManager mHttpServiceManager;
  private final NcUserZimbraBackendHttpHandler mNcUserZimbraBackendHttpHandler;

  private final SoapServiceManager mSoapServiceManager;
  private final NcSoapService mNcSoapService;
  private final GetFileHttpHandler mGetFileHttpHdlr;
  private final UploadFileHttpHandler mUploadFileHttpHandler;
  private final CreateTempAttachmentFileHttpHandler mCreateTempAttachmentFileHttpHdlr;
  private final ConnectivityTestHttpHandler mConnectivityTestHttpHandler;
  private final CloudAppTestsHttpHandler mCloudAppTestHttpHdlr;

  public ZimbraDriveExtension()
  {
    Zimbra mZimbra = new Zimbra();
    TokenManager tokenManager = new TokenManager();
    BackendUtils backendUtils = new BackendUtils(mZimbra.getProvisioning(), tokenManager);
    DriveProxy driveProxy = new DriveProxy(mZimbra.getProvisioning());
    CloudUtils cloudUtils = new CloudUtils(mZimbra.getProvisioning(), tokenManager, driveProxy);

    mSoapServiceManager = new SoapServiceManager();
    mNcSoapService = new NcSoapService(cloudUtils);

    mHttpServiceManager = new HttpServiceManager();
    mNcUserZimbraBackendHttpHandler = new NcUserZimbraBackendHttpHandler(backendUtils);
    mConnectivityTestHttpHandler = new ConnectivityTestHttpHandler();
    mUploadFileHttpHandler = new UploadFileHttpHandler(backendUtils, driveProxy);
    mGetFileHttpHdlr = new GetFileHttpHandler(cloudUtils, backendUtils);
    mCreateTempAttachmentFileHttpHdlr = new CreateTempAttachmentFileHttpHandler(cloudUtils, backendUtils);
    ConnectionTestUtils connectionTestUtils = new ConnectionTestUtils();
    mCloudAppTestHttpHdlr = new CloudAppTestsHttpHandler(backendUtils, driveProxy, connectionTestUtils);
  }

  @Override
  public String getBuildId()
  {
    return "1";
  }

  @Override
  public String getName()
  {
    return "ZimbraDrive";
  }

  /**
   * Method called by the ZAL Core to do the startup if the extension.
   *
   * @param zalExtensionController The ZAL Controller instance.
   * @param weakReference          The Zimbra class loader reference.
   */
  @Override
  public void startup(ZalExtensionController zalExtensionController, WeakReference<ClassLoader> weakReference)
  {
    try
    {
      mSoapServiceManager.register(mNcSoapService);
      mHttpServiceManager.registerHandler(mNcUserZimbraBackendHttpHandler);
      mHttpServiceManager.registerHandler(mConnectivityTestHttpHandler);
      mHttpServiceManager.registerHandler(mGetFileHttpHdlr);
      mHttpServiceManager.registerHandler(mUploadFileHttpHandler);
      mHttpServiceManager.registerHandler(mCreateTempAttachmentFileHttpHdlr);
      mHttpServiceManager.registerHandler(mCloudAppTestHttpHdlr);
      ZimbraLog.extensions.info("Loaded Zimbra Drive extension.");
    } catch( Throwable ex ) {
      ZimbraLog.extensions.error( "#######Critical Exception on Startup.#######", ex );
    }
  }

  /**
   * Method called by the ZAL Core to do the shutdown if the extension.
   */
  @Override
  public void shutdown()
  {
    mSoapServiceManager.unregister(mNcSoapService);
    mHttpServiceManager.unregisterHandler(mNcUserZimbraBackendHttpHandler);
    mHttpServiceManager.unregisterHandler(mConnectivityTestHttpHandler);
    mHttpServiceManager.unregisterHandler(mGetFileHttpHdlr);
    mHttpServiceManager.unregisterHandler(mUploadFileHttpHandler);
    mHttpServiceManager.unregisterHandler(mCreateTempAttachmentFileHttpHdlr);
    mHttpServiceManager.unregisterHandler(mCloudAppTestHttpHdlr);
    ZimbraLog.mailbox.info("Unloaded Zimbra Drive extension.");
  }
}
