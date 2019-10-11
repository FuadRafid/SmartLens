package com.muhtasim.fuadrafid.smartlens.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.muhtasim.fuadrafid.smartlens.R;
import com.muhtasim.fuadrafid.smartlens.activities.ScratchCardActivity;
import com.muhtasim.fuadrafid.smartlens.ocr.OcrDetectorProcessor;

/**
 * Created by Fuad Rafid on 8/15/2017.
 */

public class OperatorSelectDialog {
    AlertDialog dialog;
    boolean otherOp;
  public OperatorSelectDialog(final Context context)
  {
      AlertDialog.Builder mBuilder=new AlertDialog.Builder(context);
      mBuilder.setIcon(R.mipmap.icontry);
      View mView=((Activity)context).getLayoutInflater().inflate(R.layout.select_operator,null);
      mBuilder.setView(mView);
      mBuilder.setTitle("Operator Select");

      final Spinner opertorSeclect=(Spinner)mView.findViewById(R.id.opertatorSelect);
      final Button rechargeBtn=(Button)mView.findViewById(R.id.rechargeBtn);
      final EditText otherOperatorNumLength=(EditText)mView.findViewById(R.id.otherLen);
      final EditText otherOperatorPrefix=(EditText)mView.findViewById(R.id.otherPrefix);
      dialog= mBuilder.create();

      otherOp=false;
      opertorSeclect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              switch (position)
              {
                  case 0:
                      OcrDetectorProcessor.numLength=17;
                      OcrDetectorProcessor.numLength2=15;
                      OcrDetectorProcessor.prefix="*555*";
                      otherOperatorNumLength.setVisibility(View.GONE);
                      break;
                  case 1:
                      OcrDetectorProcessor.numLength=15;
                      OcrDetectorProcessor.numLength2=0;
                      OcrDetectorProcessor.prefix="*123*";
                      otherOperatorNumLength.setVisibility(View.GONE);
                      break;
                  case 2:
                      OcrDetectorProcessor.numLength=13;
                      OcrDetectorProcessor.numLength2=0;
                      OcrDetectorProcessor.prefix="*151*";
                      otherOperatorNumLength.setVisibility(View.GONE);
                      break;
                  case 3:
                      OcrDetectorProcessor.numLength=16;
                      OcrDetectorProcessor.numLength2=0;
                      OcrDetectorProcessor.prefix="*111*";
                      otherOperatorNumLength.setVisibility(View.GONE);
                      break;
                  case 4:
                      OcrDetectorProcessor.numLength=16;
                      OcrDetectorProcessor.numLength2=0;
                      OcrDetectorProcessor.prefix="*787*";
                      otherOperatorNumLength.setVisibility(View.GONE);
                      break;
                  case 5:
                      otherOperatorNumLength.setVisibility(View.VISIBLE);
                      otherOperatorPrefix.setVisibility(View.VISIBLE);
                      otherOp=true;

              }
              ScratchCardActivity.selectedOperator=position;
              ((TextView) parent.getChildAt(0)).setTextAppearance(context, R.style.ShadowText2);

          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {

          }
      });

      rechargeBtn.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              if(otherOp)
              {
                  if(otherOperatorNumLength.getText().toString().isEmpty() |
                          !otherOperatorNumLength.getText().toString().matches("[0-9]+") )
                  {
                      Toast.makeText(context, "Enter card number length", Toast.LENGTH_SHORT).show();
                      return;
                  }
                  else
                  {
                      OcrDetectorProcessor.numLength=Integer.parseInt(otherOperatorNumLength.getText().toString());
                      OcrDetectorProcessor.numLength2=Integer.parseInt(otherOperatorNumLength.getText().toString());
                  }
                  if(otherOperatorPrefix.getText().toString().isEmpty() |
                          !otherOperatorPrefix.getText().toString().matches("\\*[0-9]+\\*") )
                  {
                      Toast.makeText(context, "Enter correct prefix", Toast.LENGTH_SHORT).show();
                      return;
                  }
                  else
                  {
                      OcrDetectorProcessor.prefix=otherOperatorPrefix.getText().toString();
                      OcrDetectorProcessor.prefix=otherOperatorPrefix.getText().toString();
                  }
              }
              dialog.cancel();
              Intent intent=new Intent(context,ScratchCardActivity.class);
              if(otherOp)
              intent.putExtra("otherOp",Integer.parseInt(otherOperatorNumLength.getText().toString()));
              intent.putExtra("otherPref",otherOperatorPrefix.getText().toString());
              context.startActivity(intent);
              ((Activity) context).finish();
          }
      });
  }
  public void show()
  {
      dialog.show();
  }

}
