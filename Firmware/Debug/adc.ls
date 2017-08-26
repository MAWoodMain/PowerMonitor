   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3317                     	bsct
3318  0000               _CHANNELS:
3319  0000 12            	dc.b	18
3320  0001 11            	dc.b	17
3321  0002 10            	dc.b	16
3322  0003 0f            	dc.b	15
3323  0004 0e            	dc.b	14
3324  0005 0d            	dc.b	13
3325  0006 0c            	dc.b	12
3326  0007 0b            	dc.b	11
3327  0008 04            	dc.b	4
3328  0009               _VOLTAGE_CHANNEL:
3329  0009 16            	dc.b	22
3362                     ; 6 void ADC_INIT()
3362                     ; 7 {
3364                     	switch	.text
3365  0000               _ADC_INIT:
3369                     ; 9 	CLK_PCKENR2 |= 0x01;
3371  0000 721050c4      	bset	_CLK_PCKENR2,#0
3372                     ; 16 	ADC1_CR1 = 0x01;
3374  0004 35015340      	mov	_ADC1_CR1,#1
3375                     ; 19 	ADC1_CR2 = 0x02;
3377  0008 35025341      	mov	_ADC1_CR2,#2
3378                     ; 21 	ADC1_CR3 = 0xE0;
3380  000c 35e05342      	mov	_ADC1_CR3,#224
3381                     ; 22 }
3384  0010 81            	ret
3426                     ; 24 int readChannel(int adcChannel)
3426                     ; 25 {
3427                     	switch	.text
3428  0011               _readChannel:
3430  0011 89            	pushw	x
3431       00000000      OFST:	set	0
3434                     ; 26 	ADC1_CR3 &= ~0x1F;
3436  0012 c65342        	ld	a,_ADC1_CR3
3437  0015 a4e0          	and	a,#224
3438  0017 c75342        	ld	_ADC1_CR3,a
3439                     ; 27 	ADC1_CR3 |= (0x1F & adcChannel);
3441  001a 9f            	ld	a,xl
3442  001b a41f          	and	a,#31
3443  001d ca5342        	or	a,_ADC1_CR3
3444  0020 c75342        	ld	_ADC1_CR3,a
3445                     ; 29 	ADC1_SQR2 = 0;
3447  0023 725f534b      	clr	_ADC1_SQR2
3448                     ; 30 	ADC1_SQR3 = 0;
3450  0027 725f534c      	clr	_ADC1_SQR3
3451                     ; 31 	ADC1_SQR4 = 0;
3453  002b 725f534d      	clr	_ADC1_SQR4
3454                     ; 33 	if (adcChannel > 15)
3456  002f 9c            	rvf
3457  0030 1e01          	ldw	x,(OFST+1,sp)
3458  0032 a30010        	cpw	x,#16
3459  0035 2f18          	jrslt	L7132
3460                     ; 35 		ADC1_SQR2 = (0x01 << (adcChannel-16));
3462  0037 7b02          	ld	a,(OFST+2,sp)
3463  0039 a010          	sub	a,#16
3464  003b 5f            	clrw	x
3465  003c 4d            	tnz	a
3466  003d 2a01          	jrpl	L01
3467  003f 53            	cplw	x
3468  0040               L01:
3469  0040 97            	ld	xl,a
3470  0041 a601          	ld	a,#1
3471  0043 5d            	tnzw	x
3472  0044 2704          	jreq	L21
3473  0046               L41:
3474  0046 48            	sll	a
3475  0047 5a            	decw	x
3476  0048 26fc          	jrne	L41
3477  004a               L21:
3478  004a c7534b        	ld	_ADC1_SQR2,a
3480  004d 2034          	jra	L1232
3481  004f               L7132:
3482                     ; 37 	else if (adcChannel > 7)
3484  004f 9c            	rvf
3485  0050 1e01          	ldw	x,(OFST+1,sp)
3486  0052 a30008        	cpw	x,#8
3487  0055 2f18          	jrslt	L3232
3488                     ; 39 		ADC1_SQR3 = (0x01 << (adcChannel-8));
3490  0057 7b02          	ld	a,(OFST+2,sp)
3491  0059 a008          	sub	a,#8
3492  005b 5f            	clrw	x
3493  005c 4d            	tnz	a
3494  005d 2a01          	jrpl	L61
3495  005f 53            	cplw	x
3496  0060               L61:
3497  0060 97            	ld	xl,a
3498  0061 a601          	ld	a,#1
3499  0063 5d            	tnzw	x
3500  0064 2704          	jreq	L02
3501  0066               L22:
3502  0066 48            	sll	a
3503  0067 5a            	decw	x
3504  0068 26fc          	jrne	L22
3505  006a               L02:
3506  006a c7534c        	ld	_ADC1_SQR3,a
3508  006d 2014          	jra	L1232
3509  006f               L3232:
3510                     ; 43 		ADC1_SQR4 = (0x01 << adcChannel);
3512  006f 7b02          	ld	a,(OFST+2,sp)
3513  0071 5f            	clrw	x
3514  0072 4d            	tnz	a
3515  0073 2a01          	jrpl	L42
3516  0075 53            	cplw	x
3517  0076               L42:
3518  0076 97            	ld	xl,a
3519  0077 a601          	ld	a,#1
3520  0079 5d            	tnzw	x
3521  007a 2704          	jreq	L62
3522  007c               L03:
3523  007c 48            	sll	a
3524  007d 5a            	decw	x
3525  007e 26fc          	jrne	L03
3526  0080               L62:
3527  0080 c7534d        	ld	_ADC1_SQR4,a
3528  0083               L1232:
3529                     ; 45 	ADC1_CR1 |= 0x02;
3531  0083 72125340      	bset	_ADC1_CR1,#1
3533  0087               L1332:
3534                     ; 47 	while(!(ADC1_SR & 0x01));
3536  0087 c65343        	ld	a,_ADC1_SR
3537  008a a501          	bcp	a,#1
3538  008c 27f9          	jreq	L1332
3539                     ; 48 	return (ADC1_DRH << 8)|ADC1_DRL;
3541  008e c65344        	ld	a,_ADC1_DRH
3542  0091 5f            	clrw	x
3543  0092 97            	ld	xl,a
3544  0093 c65345        	ld	a,_ADC1_DRL
3545  0096 02            	rlwa	x,a
3548  0097 5b02          	addw	sp,#2
3549  0099 81            	ret
3583                     	xdef	_readChannel
3584                     	xdef	_ADC_INIT
3585                     	xdef	_VOLTAGE_CHANNEL
3586                     	xdef	_CHANNELS
3605                     	end
