package com.rk.bluetooth_send.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rk.bluetooth_send.MantraDto.Opts;
import com.rk.bluetooth_send.MantraDto.PidOptions;
import com.rk.bluetooth_send.R;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class IrisActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mScan;
    private TextView mValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iris);
        initView();
    }

    private void initView() {
        mScan = (Button) findViewById(R.id.mScan);
        mValue = (TextView) findViewById(R.id.mValue);

        mScan.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mScan:
                rdscan();
                break;
        }
    }

    private void rdscan() {
        try {
            String pidOption = getPIDOptions();
            Log.e("TAG", "PID OPTION : " + pidOption);
            if (pidOption != null) {
                Intent intent2 = new Intent();
                intent2.setAction("in.gov.uidai.rdservice.fp.CAPTURE");
                intent2.putExtra("PID_OPTIONS", pidOption);
                startActivityForResult(intent2, 2);
            }
        } catch (Exception e) {
            Log.e("Error", e.toString());
            show_error_alert(getString(R.string.apk_error_msg));
        }
    }

    private String getPIDOptions() {
        try {
            int fingerCount = 1;
            int fingerType = 0;
            int fingerFormat = 1;
            String pidVer = "2.0";
            String timeOut = "10000";
            String posh = "UNKNOWN";


            Opts opts = new Opts();
            opts.fCount = String.valueOf(fingerCount);
            opts.fType = String.valueOf(fingerType);
            opts.iCount = "0";
            opts.iType = "0";
            opts.pCount = "0";
            opts.pType = "0";
            opts.format = String.valueOf(fingerFormat);
            opts.pidVer = pidVer;
            opts.timeout = timeOut;
//            opts.otp = "";
            opts.posh = posh;
            String env = "P";
//            String env = "PP";
          /*  switch (rgEnv.getCheckedRadioButtonId()) {
                case R.id.rbStage:
                    env = "S";
                    break;
                case R.id.rbPreProd:
                    env = "PP";
                    break;
                case R.id.rbProd:
                    env = "P";
                    break;
            }*/
            opts.env = env;

            PidOptions pidOptions = new PidOptions();
            pidOptions.ver = pidVer;
            pidOptions.Opts = opts;

            Serializer serializer = new Persister();
            StringWriter writer = new StringWriter();
            serializer.write(pidOptions, writer);
            return writer.toString();
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
        return null;
    }

    private void show_error_alert(String msg) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 2:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (data != null) {
                            String result = data.getStringExtra("PID_DATA");
                            /*parseXmlData(result);*/

                            //  Serializer serializer = new Persister();
                            //   PidData pidData = serializer.read(PidData.class, result);
                            mValue.setText(result);

                        }
                    } catch (Exception e) {
                        Log.e("Error", "Error while deserialize pid data", e);
                    }
                }
                break;
        }
    }
/*
    private void parseXmlData(String xmlData) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new java.io.StringReader(xmlData)));
            doc.getDocumentElement().normalize();
            // System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("PidData");

            // System.out.println("----------------------------");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                // System.out.println("\nCurrent Element :" + nNode.getNodeName());
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    NamedNodeMap mapResp = eElement.getElementsByTagName("Resp").item(0).getAttributes();
                    String errorCode = ((Node) mapResp.getNamedItem("errCode")).getTextContent();
                    String errorInfo = ((Node) mapResp.getNamedItem("errInfo")).getTextContent();
                    // System.out.println("errCode   : " + errorCode);
                    // System.out.println("errorInfo   : " + errorInfo);
                    if (errorCode.equalsIgnoreCase("0")) {
                        encryptedPidXmlStr = eElement.getElementsByTagName("Data").item(0).getTextContent();
                        encryptedHmacStr = eElement.getElementsByTagName("Hmac").item(0).getTextContent();
                        encryptedSkey = eElement.getElementsByTagName("Skey").item(0).getTextContent();

                        NamedNodeMap mapSkey = eElement.getElementsByTagName("Skey").item(0).getAttributes();
                        ci = ((Node) mapSkey.getNamedItem("ci")).getTextContent();


                        NamedNodeMap mapDeviceINfo = eElement.getElementsByTagName("DeviceInfo").item(0).getAttributes();
                        dpId = ((Node) mapDeviceINfo.getNamedItem("dpId")).getTextContent();
                        mc = ((Node) mapDeviceINfo.getNamedItem("mc")).getTextContent();

                        mi = ((Node) mapDeviceINfo.getNamedItem("mi")).getTextContent();


                        rdsId = ((Node) mapDeviceINfo.getNamedItem("rdsId")).getTextContent();

                        rdsVer = ((Node) mapDeviceINfo.getNamedItem("rdsVer")).getTextContent();

                        dc = ((Node) mapDeviceINfo.getNamedItem("dc")).getTextContent();

                        NamedNodeMap mapData = eElement.getElementsByTagName("Data").item(0).getAttributes();
                        type = ((Node) mapData.getNamedItem("type")).getTextContent();

                        NodeList additionalINfo = eElement.getElementsByTagName("additional_info");
                        Element element = (Element) additionalINfo.item(0);
                        NamedNodeMap name_node = element.getElementsByTagName("Param").item(0).getAttributes();
                        srno = ((Node) name_node.getNamedItem("value")).getTextContent();
                        temp_nameValuePair = new ArrayList<>();
                        temp_nameValuePair.add(new BasicNameValuePair("encXMLPIDData", encryptedPidXmlStr));
                        temp_nameValuePair.add(new BasicNameValuePair("encryptedHmacBytes", encryptedHmacStr));
                        temp_nameValuePair.add(new BasicNameValuePair("encryptedSessionKey", encryptedSkey));
                        temp_nameValuePair.add(new BasicNameValuePair("certificateIdentifier", ci));
                        temp_nameValuePair.add(new BasicNameValuePair("rdsId", rdsId));
                        temp_nameValuePair.add(new BasicNameValuePair("rdsVer", rdsVer));
                        temp_nameValuePair.add(new BasicNameValuePair("dpId", dpId));
                        temp_nameValuePair.add(new BasicNameValuePair("dc", dc));
                        temp_nameValuePair.add(new BasicNameValuePair("mi", mi));
                        temp_nameValuePair.add(new BasicNameValuePair("mc", mc));
                        temp_nameValuePair.add(new BasicNameValuePair("srno", srno));
                        temp_nameValuePair.add(new BasicNameValuePair("bio", "Y"));
                        _is_scan_completed = true;
                        imgFinger.setBackgroundResource(R.drawable.finger_done);
//                        Log.e(TAG, "Data : " + eElement.getElementsByTagName("Data").item(0).getTextContent());
//                        Log.e(TAG, "Hmac : " + eElement.getElementsByTagName("Hmac").item(0).getTextContent());
//                        Log.e(TAG, "Skey : " + eElement.getElementsByTagName("Skey").item(0).getTextContent());
//                        Log.e(TAG, "ci   : " + ci);
//                        Log.e(TAG, "dpId : " + dpId);
//                        Log.e(TAG, "mc :" + mc);
//                        Log.e(TAG, "mi :" + mi);
//                        Log.e(TAG, "dc :" + dc);
//                        Log.e(TAG, "rdsId :" + rdsId);
//                        Log.e(TAG, "rdsVer :" + rdsVer);
//                        Log.e(TAG, "type   : " + type);
//                        Log.e(TAG, "srno: " + srno);
//                        Log.e("item length", "" + additionalINfo.getLength());
                    } else {
                        Toast.makeText(BenefBfdScanActivity.this, "" + errorInfo, Toast.LENGTH_SHORT).show();
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
*/


}
