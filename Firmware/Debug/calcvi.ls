   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3317                     .const:	section	.text
3318  0000               _ADC_COUNTS:
3319  0000 1000          	dc.w	4096
3320  0002               _PHASECAL:
3321  0002               L7622:
3322  0002 3fd99999      	dc.w	16345,-26215
3431                     ; 24 void calcVI(const unsigned int crossings)
3431                     ; 25 {
3433                     	switch	.text
3434  0000               _calcVI:
3436  0000 89            	pushw	x
3437  0001 520e          	subw	sp,#14
3438       0000000e      OFST:	set	14
3441                     ; 26 	const float SupplyVoltage = 3.3;
3443  0003 ce0018        	ldw	x,L7432+2
3444  0006 1f09          	ldw	(OFST-5,sp),x
3445  0008 ce0016        	ldw	x,L7432
3446  000b 1f07          	ldw	(OFST-7,sp),x
3447                     ; 27   unsigned int crossCount = 0;
3449  000d 5f            	clrw	x
3450  000e 1f05          	ldw	(OFST-9,sp),x
3451                     ; 28   unsigned int numberOfSamples = 0;
3453  0010 5f            	clrw	x
3454  0011 1f0b          	ldw	(OFST-3,sp),x
3455                     ; 33   sumV = 0;
3457  0013 ae0000        	ldw	x,#0
3458  0016 bf50          	ldw	_sumV+2,x
3459  0018 ae0000        	ldw	x,#0
3460  001b bf4e          	ldw	_sumV,x
3461                     ; 34 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3463  001d 5f            	clrw	x
3464  001e 1f0d          	ldw	(OFST-1,sp),x
3465  0020               L3532:
3466                     ; 36 		sumI[i] = 0;
3468  0020 1e0d          	ldw	x,(OFST-1,sp)
3469  0022 58            	sllw	x
3470  0023 58            	sllw	x
3471  0024 a600          	ld	a,#0
3472  0026 e729          	ld	(_sumI+3,x),a
3473  0028 a600          	ld	a,#0
3474  002a e728          	ld	(_sumI+2,x),a
3475  002c a600          	ld	a,#0
3476  002e e727          	ld	(_sumI+1,x),a
3477  0030 a600          	ld	a,#0
3478  0032 e726          	ld	(_sumI,x),a
3479                     ; 37 		sumP[i] = 0;
3481  0034 1e0d          	ldw	x,(OFST-1,sp)
3482  0036 58            	sllw	x
3483  0037 58            	sllw	x
3484  0038 a600          	ld	a,#0
3485  003a e705          	ld	(_sumP+3,x),a
3486  003c a600          	ld	a,#0
3487  003e e704          	ld	(_sumP+2,x),a
3488  0040 a600          	ld	a,#0
3489  0042 e703          	ld	(_sumP+1,x),a
3490  0044 a600          	ld	a,#0
3491  0046 e702          	ld	(_sumP,x),a
3492                     ; 34 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3494  0048 1e0d          	ldw	x,(OFST-1,sp)
3495  004a 1c0001        	addw	x,#1
3496  004d 1f0d          	ldw	(OFST-1,sp),x
3499  004f 9c            	rvf
3500  0050 1e0d          	ldw	x,(OFST-1,sp)
3501  0052 a30009        	cpw	x,#9
3502  0055 2fc9          	jrslt	L3532
3503  0057               L1632:
3504                     ; 42     startV = readChannel(VOLTAGE_CHANNEL);
3506  0057 b600          	ld	a,_VOLTAGE_CHANNEL
3507  0059 5f            	clrw	x
3508  005a 97            	ld	xl,a
3509  005b cd0000        	call	_readChannel
3511  005e bf00          	ldw	_startV,x
3512                     ; 44 	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
3514  0060 9c            	rvf
3515  0061 be00          	ldw	x,_startV
3516  0063 cd0000        	call	c_itof
3518  0066 ae0012        	ldw	x,#L3732
3519  0069 cd0000        	call	c_fcmp
3521  006c 2ee9          	jrsge	L1632
3523  006e 9c            	rvf
3524  006f be00          	ldw	x,_startV
3525  0071 cd0000        	call	c_itof
3527  0074 ae000e        	ldw	x,#L3042
3528  0077 cd0000        	call	c_fcmp
3530  007a 2ddb          	jrsle	L1632
3532  007c acdb01db      	jpf	L1142
3533  0080               L7042:
3534                     ; 49     numberOfSamples++;                       //Count number of times looped.
3536  0080 1e0b          	ldw	x,(OFST-3,sp)
3537  0082 1c0001        	addw	x,#1
3538  0085 1f0b          	ldw	(OFST-3,sp),x
3539                     ; 50     lastFilteredV = filteredV;               //Used for delay/phase compensation
3541  0087 be64          	ldw	x,_filteredV+2
3542  0089 bf68          	ldw	_lastFilteredV+2,x
3543  008b be62          	ldw	x,_filteredV
3544  008d bf66          	ldw	_lastFilteredV,x
3545                     ; 56     sampleV = readChannel(VOLTAGE_CHANNEL);
3547  008f b600          	ld	a,_VOLTAGE_CHANNEL
3548  0091 5f            	clrw	x
3549  0092 97            	ld	xl,a
3550  0093 cd0000        	call	_readChannel
3552  0096 bf7c          	ldw	_sampleV,x
3553                     ; 58 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3555  0098 5f            	clrw	x
3556  0099 1f0d          	ldw	(OFST-1,sp),x
3557  009b               L5142:
3558                     ; 60 			sampleI[i] = readChannel(CHANNELS[i]);
3560  009b 1e0d          	ldw	x,(OFST-1,sp)
3561  009d e600          	ld	a,(_CHANNELS,x)
3562  009f 5f            	clrw	x
3563  00a0 97            	ld	xl,a
3564  00a1 cd0000        	call	_readChannel
3566  00a4 160d          	ldw	y,(OFST-1,sp)
3567  00a6 9058          	sllw	y
3568  00a8 90ef6a        	ldw	(_sampleI,y),x
3569                     ; 58 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3571  00ab 1e0d          	ldw	x,(OFST-1,sp)
3572  00ad 1c0001        	addw	x,#1
3573  00b0 1f0d          	ldw	(OFST-1,sp),x
3576  00b2 9c            	rvf
3577  00b3 1e0d          	ldw	x,(OFST-1,sp)
3578  00b5 a30009        	cpw	x,#9
3579  00b8 2fe1          	jrslt	L5142
3580                     ; 68     offsetV = offsetV + ((sampleV-offsetV)/1024);
3582  00ba be7c          	ldw	x,_sampleV
3583  00bc cd0000        	call	c_itof
3585  00bf ae005a        	ldw	x,#_offsetV
3586  00c2 cd0000        	call	c_fsub
3588  00c5 ae000a        	ldw	x,#L7242
3589  00c8 cd0000        	call	c_fdiv
3591  00cb ae005a        	ldw	x,#_offsetV
3592  00ce cd0000        	call	c_fgadd
3594                     ; 69     filteredV = sampleV - offsetV;
3596  00d1 be7c          	ldw	x,_sampleV
3597  00d3 cd0000        	call	c_itof
3599  00d6 ae005a        	ldw	x,#_offsetV
3600  00d9 cd0000        	call	c_fsub
3602  00dc ae0062        	ldw	x,#_filteredV
3603  00df cd0000        	call	c_rtol
3605                     ; 70     sumV += filteredV * filteredV;
3607  00e2 ae0062        	ldw	x,#_filteredV
3608  00e5 cd0000        	call	c_ltor
3610  00e8 ae0062        	ldw	x,#_filteredV
3611  00eb cd0000        	call	c_fmul
3613  00ee ae004e        	ldw	x,#_sumV
3614  00f1 cd0000        	call	c_fgadd
3616                     ; 71     phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
3618  00f4 ae0062        	ldw	x,#_filteredV
3619  00f7 cd0000        	call	c_ltor
3621  00fa ae0066        	ldw	x,#_lastFilteredV
3622  00fd cd0000        	call	c_fsub
3624  0100 ae0002        	ldw	x,#L7622
3625  0103 cd0000        	call	c_fmul
3627  0106 ae0066        	ldw	x,#_lastFilteredV
3628  0109 cd0000        	call	c_fadd
3630  010c ae0052        	ldw	x,#_phaseShiftedV
3631  010f cd0000        	call	c_rtol
3633                     ; 73 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3635  0112 5f            	clrw	x
3636  0113 1f0d          	ldw	(OFST-1,sp),x
3637  0115               L3342:
3638                     ; 75 			offsetI = offsetI + ((sampleI[i]-offsetI)/1024);
3640  0115 1e0d          	ldw	x,(OFST-1,sp)
3641  0117 58            	sllw	x
3642  0118 ee6a          	ldw	x,(_sampleI,x)
3643  011a cd0000        	call	c_itof
3645  011d ae0056        	ldw	x,#_offsetI
3646  0120 cd0000        	call	c_fsub
3648  0123 ae000a        	ldw	x,#L7242
3649  0126 cd0000        	call	c_fdiv
3651  0129 ae0056        	ldw	x,#_offsetI
3652  012c cd0000        	call	c_fgadd
3654                     ; 76 			filteredI = sampleI[i] - offsetI;
3656  012f 1e0d          	ldw	x,(OFST-1,sp)
3657  0131 58            	sllw	x
3658  0132 ee6a          	ldw	x,(_sampleI,x)
3659  0134 cd0000        	call	c_itof
3661  0137 ae0056        	ldw	x,#_offsetI
3662  013a cd0000        	call	c_fsub
3664  013d ae005e        	ldw	x,#_filteredI
3665  0140 cd0000        	call	c_rtol
3667                     ; 77 			sumI[i] += filteredI * filteredI;
3669  0143 ae005e        	ldw	x,#_filteredI
3670  0146 cd0000        	call	c_ltor
3672  0149 ae005e        	ldw	x,#_filteredI
3673  014c cd0000        	call	c_fmul
3675  014f 1e0d          	ldw	x,(OFST-1,sp)
3676  0151 58            	sllw	x
3677  0152 58            	sllw	x
3678  0153 1c0026        	addw	x,#_sumI
3679  0156 cd0000        	call	c_fgadd
3681                     ; 79 			instP = phaseShiftedV * filteredI;
3683  0159 ae0052        	ldw	x,#_phaseShiftedV
3684  015c cd0000        	call	c_ltor
3686  015f ae005e        	ldw	x,#_filteredI
3687  0162 cd0000        	call	c_fmul
3689  0165 ae004a        	ldw	x,#_instP
3690  0168 cd0000        	call	c_rtol
3692                     ; 80 			sumP[i] +=instP;
3694  016b 1e0d          	ldw	x,(OFST-1,sp)
3695  016d 58            	sllw	x
3696  016e 58            	sllw	x
3697  016f b64d          	ld	a,_instP+3
3698  0171 b703          	ld	c_lreg+3,a
3699  0173 b64c          	ld	a,_instP+2
3700  0175 b702          	ld	c_lreg+2,a
3701  0177 b64b          	ld	a,_instP+1
3702  0179 b701          	ld	c_lreg+1,a
3703  017b b64a          	ld	a,_instP
3704  017d b700          	ld	c_lreg,a
3705  017f 1c0002        	addw	x,#_sumP
3706  0182 cd0000        	call	c_fgadd
3708                     ; 73 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3710  0185 1e0d          	ldw	x,(OFST-1,sp)
3711  0187 1c0001        	addw	x,#1
3712  018a 1f0d          	ldw	(OFST-1,sp),x
3715  018c 9c            	rvf
3716  018d 1e0d          	ldw	x,(OFST-1,sp)
3717  018f a30009        	cpw	x,#9
3718  0192 2e03          	jrsge	L61
3719  0194 cc0115        	jp	L3342
3720  0197               L61:
3721                     ; 88     lastVCross = checkVCross;
3723                     	btst		_checkVCross
3724  019c 90110001      	bccm	_lastVCross
3725                     ; 90 		checkVCross = sampleV > startV;
3727  01a0 9c            	rvf
3728  01a1 be7c          	ldw	x,_sampleV
3729  01a3 b300          	cpw	x,_startV
3730  01a5 2c02          	jrsgt	L02
3731  01a7 2006          	jp	L6
3732  01a9               L02:
3733  01a9 72100000      	bset	_checkVCross
3734  01ad 2004          	jra	L01
3735  01af               L6:
3736  01af 72110000      	bres	_checkVCross
3737  01b3               L01:
3738                     ; 95     if (numberOfSamples==1) lastVCross = checkVCross;
3740  01b3 1e0b          	ldw	x,(OFST-3,sp)
3741  01b5 a30001        	cpw	x,#1
3742  01b8 2609          	jrne	L1442
3745                     	btst		_checkVCross
3746  01bf 90110001      	bccm	_lastVCross
3747  01c3               L1442:
3748                     ; 97     if (lastVCross != checkVCross) crossCount++;
3750  01c3 7201000107    	btjf	_lastVCross,L21
3751  01c8 720000000e    	btjt	_checkVCross,L1142
3752  01cd 2005          	jra	L41
3753  01cf               L21:
3754  01cf 7201000007    	btjf	_checkVCross,L1142
3755  01d4               L41:
3758  01d4 1e05          	ldw	x,(OFST-9,sp)
3759  01d6 1c0001        	addw	x,#1
3760  01d9 1f05          	ldw	(OFST-9,sp),x
3761  01db               L1142:
3762                     ; 46 	while(crossCount < crossings)
3764  01db 1e05          	ldw	x,(OFST-9,sp)
3765  01dd 130f          	cpw	x,(OFST+1,sp)
3766  01df 2403          	jruge	L22
3767  01e1 cc0080        	jp	L7042
3768  01e4               L22:
3769                     ; 109 	RATIO = SupplyVoltage / ADC_COUNTS;
3771  01e4 96            	ldw	x,sp
3772  01e5 1c0007        	addw	x,#OFST-7
3773  01e8 cd0000        	call	c_ltor
3775  01eb ae0006        	ldw	x,#L1542
3776  01ee cd0000        	call	c_fdiv
3778  01f1 96            	ldw	x,sp
3779  01f2 1c0007        	addw	x,#OFST-7
3780  01f5 cd0000        	call	c_rtol
3782                     ; 110   Vrms = RATIO * sqrt(sumV / numberOfSamples);
3784  01f8 1e0b          	ldw	x,(OFST-3,sp)
3785  01fa cd0000        	call	c_uitof
3787  01fd 96            	ldw	x,sp
3788  01fe 1c0001        	addw	x,#OFST-13
3789  0201 cd0000        	call	c_rtol
3791  0204 ae004e        	ldw	x,#_sumV
3792  0207 cd0000        	call	c_ltor
3794  020a 96            	ldw	x,sp
3795  020b 1c0001        	addw	x,#OFST-13
3796  020e cd0000        	call	c_fdiv
3798  0211 be02          	ldw	x,c_lreg+2
3799  0213 89            	pushw	x
3800  0214 be00          	ldw	x,c_lreg
3801  0216 89            	pushw	x
3802  0217 cd0000        	call	_sqrt
3804  021a 5b04          	addw	sp,#4
3805  021c 96            	ldw	x,sp
3806  021d 1c0007        	addw	x,#OFST-7
3807  0220 cd0000        	call	c_fmul
3809  0223 ae00c6        	ldw	x,#_Vrms
3810  0226 cd0000        	call	c_rtol
3812                     ; 112 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3814  0229 5f            	clrw	x
3815  022a 1f0d          	ldw	(OFST-1,sp),x
3816  022c               L5542:
3817                     ; 114 		Irms[i] = RATIO * sqrt(sumI[i] / numberOfSamples);
3819  022c 1e0b          	ldw	x,(OFST-3,sp)
3820  022e cd0000        	call	c_uitof
3822  0231 96            	ldw	x,sp
3823  0232 1c0001        	addw	x,#OFST-13
3824  0235 cd0000        	call	c_rtol
3826  0238 1e0d          	ldw	x,(OFST-1,sp)
3827  023a 58            	sllw	x
3828  023b 58            	sllw	x
3829  023c 1c0026        	addw	x,#_sumI
3830  023f cd0000        	call	c_ltor
3832  0242 96            	ldw	x,sp
3833  0243 1c0001        	addw	x,#OFST-13
3834  0246 cd0000        	call	c_fdiv
3836  0249 be02          	ldw	x,c_lreg+2
3837  024b 89            	pushw	x
3838  024c be00          	ldw	x,c_lreg
3839  024e 89            	pushw	x
3840  024f cd0000        	call	_sqrt
3842  0252 5b04          	addw	sp,#4
3843  0254 96            	ldw	x,sp
3844  0255 1c0007        	addw	x,#OFST-7
3845  0258 cd0000        	call	c_fmul
3847  025b 1e0d          	ldw	x,(OFST-1,sp)
3848  025d 58            	sllw	x
3849  025e 58            	sllw	x
3850  025f 1c007e        	addw	x,#_Irms
3851  0262 cd0000        	call	c_rtol
3853                     ; 115 		realPower[i] = RATIO * RATIO * sumP[i] / numberOfSamples;
3855  0265 1e0b          	ldw	x,(OFST-3,sp)
3856  0267 cd0000        	call	c_uitof
3858  026a 96            	ldw	x,sp
3859  026b 1c0001        	addw	x,#OFST-13
3860  026e cd0000        	call	c_rtol
3862  0271 96            	ldw	x,sp
3863  0272 1c0007        	addw	x,#OFST-7
3864  0275 cd0000        	call	c_ltor
3866  0278 96            	ldw	x,sp
3867  0279 1c0007        	addw	x,#OFST-7
3868  027c cd0000        	call	c_fmul
3870  027f 1e0d          	ldw	x,(OFST-1,sp)
3871  0281 58            	sllw	x
3872  0282 58            	sllw	x
3873  0283 1c0002        	addw	x,#_sumP
3874  0286 cd0000        	call	c_fmul
3876  0289 96            	ldw	x,sp
3877  028a 1c0001        	addw	x,#OFST-13
3878  028d cd0000        	call	c_fdiv
3880  0290 1e0d          	ldw	x,(OFST-1,sp)
3881  0292 58            	sllw	x
3882  0293 58            	sllw	x
3883  0294 1c00a2        	addw	x,#_realPower
3884  0297 cd0000        	call	c_rtol
3886                     ; 112 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3888  029a 1e0d          	ldw	x,(OFST-1,sp)
3889  029c 1c0001        	addw	x,#1
3890  029f 1f0d          	ldw	(OFST-1,sp),x
3893  02a1 9c            	rvf
3894  02a2 1e0d          	ldw	x,(OFST-1,sp)
3895  02a4 a30009        	cpw	x,#9
3896  02a7 2e02          	jrsge	L42
3897  02a9 2081          	jp	L5542
3898  02ab               L42:
3899                     ; 121 }
3902  02ab 5b10          	addw	sp,#16
3903  02ad 81            	ret
3938                     ; 123 float getRealPower(unsigned int channelNo)
3938                     ; 124 {
3939                     	switch	.text
3940  02ae               _getRealPower:
3944                     ; 125 	return realPower[channelNo];
3946  02ae 58            	sllw	x
3947  02af 58            	sllw	x
3948  02b0 1c00a2        	addw	x,#_realPower
3949  02b3 cd0000        	call	c_ltor
3953  02b6 81            	ret
3988                     ; 128 float getIrms(unsigned int channelNo)
3988                     ; 129 {
3989                     	switch	.text
3990  02b7               _getIrms:
3994                     ; 130 	return Irms[channelNo];
3996  02b7 58            	sllw	x
3997  02b8 58            	sllw	x
3998  02b9 1c007e        	addw	x,#_Irms
3999  02bc cd0000        	call	c_ltor
4003  02bf 81            	ret
4027                     ; 133 float getVrms()
4027                     ; 134 {
4028                     	switch	.text
4029  02c0               _getVrms:
4033                     ; 135 	return Vrms;
4035  02c0 ae00c6        	ldw	x,#_Vrms
4036  02c3 cd0000        	call	c_ltor
4040  02c6 81            	ret
4242                     .bit:	section	.data,bit
4243  0000               _checkVCross:
4244  0000 00            	ds.b	1
4245                     	xdef	_checkVCross
4246  0001               _lastVCross:
4247  0001 00            	ds.b	1
4248                     	xdef	_lastVCross
4249                     	switch	.ubsct
4250  0000               _startV:
4251  0000 0000          	ds.b	2
4252                     	xdef	_startV
4253  0002               _sumP:
4254  0002 000000000000  	ds.b	36
4255                     	xdef	_sumP
4256  0026               _sumI:
4257  0026 000000000000  	ds.b	36
4258                     	xdef	_sumI
4259  004a               _instP:
4260  004a 00000000      	ds.b	4
4261                     	xdef	_instP
4262  004e               _sumV:
4263  004e 00000000      	ds.b	4
4264                     	xdef	_sumV
4265  0052               _phaseShiftedV:
4266  0052 00000000      	ds.b	4
4267                     	xdef	_phaseShiftedV
4268  0056               _offsetI:
4269  0056 00000000      	ds.b	4
4270                     	xdef	_offsetI
4271  005a               _offsetV:
4272  005a 00000000      	ds.b	4
4273                     	xdef	_offsetV
4274  005e               _filteredI:
4275  005e 00000000      	ds.b	4
4276                     	xdef	_filteredI
4277  0062               _filteredV:
4278  0062 00000000      	ds.b	4
4279                     	xdef	_filteredV
4280  0066               _lastFilteredV:
4281  0066 00000000      	ds.b	4
4282                     	xdef	_lastFilteredV
4283  006a               _sampleI:
4284  006a 000000000000  	ds.b	18
4285                     	xdef	_sampleI
4286  007c               _sampleV:
4287  007c 0000          	ds.b	2
4288                     	xdef	_sampleV
4289  007e               _Irms:
4290  007e 000000000000  	ds.b	36
4291                     	xdef	_Irms
4292  00a2               _realPower:
4293  00a2 000000000000  	ds.b	36
4294                     	xdef	_realPower
4295  00c6               _Vrms:
4296  00c6 00000000      	ds.b	4
4297                     	xdef	_Vrms
4298                     	xdef	_getRealPower
4299                     	xdef	_getIrms
4300                     	xdef	_getVrms
4301                     	xdef	_calcVI
4302                     	xdef	_PHASECAL
4303                     	xdef	_ADC_COUNTS
4304                     	xref	_readChannel
4305                     	xref.b	_VOLTAGE_CHANNEL
4306                     	xref.b	_CHANNELS
4307                     	xref	_sqrt
4308                     	switch	.const
4309  0006               L1542:
4310  0006 45800000      	dc.w	17792,0
4311  000a               L7242:
4312  000a 44800000      	dc.w	17536,0
4313  000e               L3042:
4314  000e 44e66666      	dc.w	17638,26214
4315  0012               L3732:
4316  0012 450ccccc      	dc.w	17676,-13108
4317  0016               L7432:
4318  0016 40533333      	dc.w	16467,13107
4319                     	xref.b	c_lreg
4320                     	xref.b	c_x
4340                     	xref	c_uitof
4341                     	xref	c_fadd
4342                     	xref	c_fmul
4343                     	xref	c_ltor
4344                     	xref	c_rtol
4345                     	xref	c_fgadd
4346                     	xref	c_fdiv
4347                     	xref	c_fsub
4348                     	xref	c_fcmp
4349                     	xref	c_itof
4350                     	end
