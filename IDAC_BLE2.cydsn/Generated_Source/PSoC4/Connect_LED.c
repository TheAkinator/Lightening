/*******************************************************************************
* File Name: Connect_LED.c  
* Version 2.10
*
* Description:
*  This file contains API to enable firmware control of a Pins component.
*
* Note:
*
********************************************************************************
* Copyright 2008-2014, Cypress Semiconductor Corporation.  All rights reserved.
* You may use this file only in accordance with the license, terms, conditions, 
* disclaimers, and limitations in the end user license agreement accompanying 
* the software package with which this file was provided.
*******************************************************************************/

#include "cytypes.h"
#include "Connect_LED.h"

#define SetP4PinDriveMode(shift, mode)  \
    do { \
        Connect_LED_PC =   (Connect_LED_PC & \
                                (uint32)(~(uint32)(Connect_LED_DRIVE_MODE_IND_MASK << (Connect_LED_DRIVE_MODE_BITS * (shift))))) | \
                                (uint32)((uint32)(mode) << (Connect_LED_DRIVE_MODE_BITS * (shift))); \
    } while (0)


/*******************************************************************************
* Function Name: Connect_LED_Write
********************************************************************************
*
* Summary:
*  Assign a new value to the digital port's data output register.  
*
* Parameters:  
*  prtValue:  The value to be assigned to the Digital Port. 
*
* Return: 
*  None 
*  
*******************************************************************************/
void Connect_LED_Write(uint8 value) 
{
    uint8 drVal = (uint8)(Connect_LED_DR & (uint8)(~Connect_LED_MASK));
    drVal = (drVal | ((uint8)(value << Connect_LED_SHIFT) & Connect_LED_MASK));
    Connect_LED_DR = (uint32)drVal;
}


/*******************************************************************************
* Function Name: Connect_LED_SetDriveMode
********************************************************************************
*
* Summary:
*  Change the drive mode on the pins of the port.
* 
* Parameters:  
*  mode:  Change the pins to one of the following drive modes.
*
*  Connect_LED_DM_STRONG     Strong Drive 
*  Connect_LED_DM_OD_HI      Open Drain, Drives High 
*  Connect_LED_DM_OD_LO      Open Drain, Drives Low 
*  Connect_LED_DM_RES_UP     Resistive Pull Up 
*  Connect_LED_DM_RES_DWN    Resistive Pull Down 
*  Connect_LED_DM_RES_UPDWN  Resistive Pull Up/Down 
*  Connect_LED_DM_DIG_HIZ    High Impedance Digital 
*  Connect_LED_DM_ALG_HIZ    High Impedance Analog 
*
* Return: 
*  None
*
*******************************************************************************/
void Connect_LED_SetDriveMode(uint8 mode) 
{
	SetP4PinDriveMode(Connect_LED__0__SHIFT, mode);
}


/*******************************************************************************
* Function Name: Connect_LED_Read
********************************************************************************
*
* Summary:
*  Read the current value on the pins of the Digital Port in right justified 
*  form.
*
* Parameters:  
*  None 
*
* Return: 
*  Returns the current value of the Digital Port as a right justified number
*  
* Note:
*  Macro Connect_LED_ReadPS calls this function. 
*  
*******************************************************************************/
uint8 Connect_LED_Read(void) 
{
    return (uint8)((Connect_LED_PS & Connect_LED_MASK) >> Connect_LED_SHIFT);
}


/*******************************************************************************
* Function Name: Connect_LED_ReadDataReg
********************************************************************************
*
* Summary:
*  Read the current value assigned to a Digital Port's data output register
*
* Parameters:  
*  None 
*
* Return: 
*  Returns the current value assigned to the Digital Port's data output register
*  
*******************************************************************************/
uint8 Connect_LED_ReadDataReg(void) 
{
    return (uint8)((Connect_LED_DR & Connect_LED_MASK) >> Connect_LED_SHIFT);
}


/* If Interrupts Are Enabled for this Pins component */ 
#if defined(Connect_LED_INTSTAT) 

    /*******************************************************************************
    * Function Name: Connect_LED_ClearInterrupt
    ********************************************************************************
    *
    * Summary:
    *  Clears any active interrupts attached to port and returns the value of the 
    *  interrupt status register.
    *
    * Parameters:  
    *  None 
    *
    * Return: 
    *  Returns the value of the interrupt status register
    *  
    *******************************************************************************/
    uint8 Connect_LED_ClearInterrupt(void) 
    {
		uint8 maskedStatus = (uint8)(Connect_LED_INTSTAT & Connect_LED_MASK);
		Connect_LED_INTSTAT = maskedStatus;
        return maskedStatus >> Connect_LED_SHIFT;
    }

#endif /* If Interrupts Are Enabled for this Pins component */ 


/* [] END OF FILE */
