/* ========================================
 *
 * Copyright YOUR COMPANY, THE YEAR
 * All Rights Reserved
 * UNPUBLISHED, LICENSED SOFTWARE.
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION
 * WHICH IS THE PROPERTY OF your company.
 *
 * ========================================
*/

#include <project.h>
#include <stdio.h>

#define LED_OFF  (1u)
#define LED_ON  (0u)

void StackEventHandler( uint32 eventCode, void *eventParam );
uint8 IDACValue;
uint8 MotorValue = 0;
CYBLE_GATT_HANDLE_VALUE_PAIR_T		IDACHandle;

unsigned char motor_phases[8] = {0x8,0xC,0x4,0x6,0x2,0x3,0x1,0x9};

void
motor_step(int dir)
{
    static int phase = 0;
    
    Pin_1_Write(motor_phases[phase]);
    phase = phase + dir + 8;
    phase = phase & 0x7;
}

void LedVerify()
{
    /*if Disconnected TURN ON RED LED*/
    if(CyBle_GetState()==CYBLE_STATE_DISCONNECTED)
    {
        Advertising_LED_Write(LED_OFF);
        Disconnect_LED_Write(LED_ON);
        Connect_LED_Write(LED_OFF);
    }
    /*if Advertising TURN ON BLUE LED*/
    else if(CyBle_GetState()==CYBLE_STATE_ADVERTISING)
    {
        Advertising_LED_Write(LED_ON);
        Disconnect_LED_Write(LED_OFF);
        Connect_LED_Write(LED_OFF);
    }
    /*If Connected TURN ON GREEN LED*/
    else if(CyBle_GetState()==CYBLE_STATE_CONNECTED)
    {
        Advertising_LED_Write(LED_OFF);
        Disconnect_LED_Write(LED_OFF);
        Connect_LED_Write(LED_OFF);
    }
}


int main()
{
    CyGlobalIntEnable;   /* Enable global interrupts */
    
    /* Place your initialization/startup code here (e.g. MyInst_Start()) */
    IDAC_Start();
    CyBle_Start( StackEventHandler );
    
    for(;;)
    {
        LedVerify();
        switch(IDACValue)
        {
            case 0:
             Disconnect_LED_Write(LED_ON);
            break;
            case 1:
            Advertising_LED_Write(LED_ON);
            break;
            case 200:
             Connect_LED_Write(LED_ON);
            break;
        }
        
        if(MotorValue == 1){
        int i;
        
        for (i = 0; i < 10000; ++i)
        {
            motor_step(1);
            CyDelay(1);
        }
        
        MotorValue = 0;
        Pin_1_Write(0x0);
        }
        
         if(MotorValue == 2){
        int i;
        
        for (i = 0; i < 10000; ++i)
        {
            motor_step(-1);
            CyDelay(1);
        }
        
        MotorValue = 0;
        Pin_1_Write(0x0);
        }
        /* Place your application code here */
        CyBle_ProcessEvents();
    }
}

void StackEventHandler( uint32 eventCode, void *eventParam )
{

    CYBLE_GATTS_WRITE_REQ_PARAM_T *wrReqParam;
        
    switch(eventCode)
    {
       
 
              case CYBLE_EVT_STACK_ON:
        case CYBLE_EVT_GAP_DEVICE_DISCONNECTED:
            /* Start BLE advertisement for 30 seconds and update link
             * status on LEDs */
            CyBle_GappStartAdvertisement(CYBLE_ADVERTISING_FAST);
            Advertising_LED_Write(LED_OFF);
            
        break;

        case CYBLE_EVT_GAP_DEVICE_CONNECTED:
            /* BLE link is established */
            Advertising_LED_Write(LED_OFF);
            Disconnect_LED_Write(LED_OFF);
        break;

        case CYBLE_EVT_GAPP_ADVERTISEMENT_START_STOP:
            if(CyBle_GetState() == CYBLE_STATE_DISCONNECTED)
            {
                /* Advertisement event timed out, go to low power
                 * mode (Stop mode) and wait for device reset
                 * event to wake up the device again */
                Advertising_LED_Write(LED_ON);
                Disconnect_LED_Write(LED_OFF);
                CySysPmSetWakeupPolarity(CY_PM_STOP_WAKEUP_ACTIVE_HIGH);
                CySysPmStop();
               
                /* Code execution will not reach here */
            }
        break;
                
        case CYBLE_EVT_GATTS_WRITE_REQ: 							

            
            wrReqParam = (CYBLE_GATTS_WRITE_REQ_PARAM_T *) eventParam;
            
               if(wrReqParam->handleValPair.attrHandle == cyBle_customs[CYBLE_IDAC_SERVICE_INDEX].customServInfo[CYBLE_IDAC_CHANGE_IDAC_CHAR_INDEX].customServiceCharHandle){
                IDACValue = wrReqParam->handleValPair.value.val[0];
                
               IDAC_SetValue(IDACValue);
               
        	
        	/* Update RGB control handle with new values */
        	IDACHandle.attrHandle = CYBLE_IDAC_CHANGE_IDAC_CHAR_HANDLE;
        	IDACHandle.value.val = &IDACValue;
        	IDACHandle.value.len = sizeof(uint8);

        	
        	/* Send updated RGB control handle as attribute for read by central device */
        	CyBle_GattsWriteAttributeValue(&IDACHandle, 0, &cyBle_connHandle, 0); 
            

			CyBle_GattsWriteRsp(cyBle_connHandle);
            
            }
            
             if(wrReqParam->handleValPair.attrHandle == cyBle_customs[CYBLE_IDAC_SERVICE_INDEX].customServInfo[CYBLE_IDAC_START_MOTOR_CHAR_INDEX].customServiceCharHandle){
                MotorValue = wrReqParam->handleValPair.value.val[0];
                
              
               
        	
        	
        	IDACHandle.attrHandle = CYBLE_IDAC_START_MOTOR_CHAR_HANDLE;
        	IDACHandle.value.val = &MotorValue;
        	IDACHandle.value.len = sizeof(uint8);

        	
        	
        	CyBle_GattsWriteAttributeValue(&IDACHandle, 0, &cyBle_connHandle, 0); 
            

			CyBle_GattsWriteRsp(cyBle_connHandle);
            
            } 
			
			break;
          
        default:
            break;
        
    }
}

