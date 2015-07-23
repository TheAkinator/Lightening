/*******************************************************************************
* File Name: MyPin.h  
* Version 2.10
*
* Description:
*  This file containts Control Register function prototypes and register defines
*
* Note:
*
********************************************************************************
* Copyright 2008-2014, Cypress Semiconductor Corporation.  All rights reserved.
* You may use this file only in accordance with the license, terms, conditions, 
* disclaimers, and limitations in the end user license agreement accompanying 
* the software package with which this file was provided.
*******************************************************************************/

#if !defined(CY_PINS_MyPin_H) /* Pins MyPin_H */
#define CY_PINS_MyPin_H

#include "cytypes.h"
#include "cyfitter.h"
#include "MyPin_aliases.h"


/***************************************
*        Function Prototypes             
***************************************/    

void    MyPin_Write(uint8 value) ;
void    MyPin_SetDriveMode(uint8 mode) ;
uint8   MyPin_ReadDataReg(void) ;
uint8   MyPin_Read(void) ;
uint8   MyPin_ClearInterrupt(void) ;


/***************************************
*           API Constants        
***************************************/

/* Drive Modes */
#define MyPin_DRIVE_MODE_BITS        (3)
#define MyPin_DRIVE_MODE_IND_MASK    (0xFFFFFFFFu >> (32 - MyPin_DRIVE_MODE_BITS))

#define MyPin_DM_ALG_HIZ         (0x00u)
#define MyPin_DM_DIG_HIZ         (0x01u)
#define MyPin_DM_RES_UP          (0x02u)
#define MyPin_DM_RES_DWN         (0x03u)
#define MyPin_DM_OD_LO           (0x04u)
#define MyPin_DM_OD_HI           (0x05u)
#define MyPin_DM_STRONG          (0x06u)
#define MyPin_DM_RES_UPDWN       (0x07u)

/* Digital Port Constants */
#define MyPin_MASK               MyPin__MASK
#define MyPin_SHIFT              MyPin__SHIFT
#define MyPin_WIDTH              1u


/***************************************
*             Registers        
***************************************/

/* Main Port Registers */
/* Pin State */
#define MyPin_PS                     (* (reg32 *) MyPin__PS)
/* Port Configuration */
#define MyPin_PC                     (* (reg32 *) MyPin__PC)
/* Data Register */
#define MyPin_DR                     (* (reg32 *) MyPin__DR)
/* Input Buffer Disable Override */
#define MyPin_INP_DIS                (* (reg32 *) MyPin__PC2)


#if defined(MyPin__INTSTAT)  /* Interrupt Registers */

    #define MyPin_INTSTAT                (* (reg32 *) MyPin__INTSTAT)

#endif /* Interrupt Registers */


/***************************************
* The following code is DEPRECATED and 
* must not be used.
***************************************/

#define MyPin_DRIVE_MODE_SHIFT       (0x00u)
#define MyPin_DRIVE_MODE_MASK        (0x07u << MyPin_DRIVE_MODE_SHIFT)


#endif /* End Pins MyPin_H */


/* [] END OF FILE */
