   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.11.14 - 18 Nov 2019
   3                     ; Generator (Limited) V4.4.11 - 19 Nov 2019
3316                     	bsct
3317  0000               _offsetV:
3318  0000 45000000      	dc.w	17664,0
3319  0004               _offsetI:
3320  0004 45000000      	dc.w	17664,0
3418                     ; 20 void calcVI(const unsigned int crossings)
3418                     ; 21 {
3420                     	switch	.text
3421  0000               _calcVI:
3423  0000 89            	pushw	x
3424  0001 520e          	subw	sp,#14
3425       0000000e      OFST:	set	14
3428                     ; 22     unsigned int crossCount = 0;
3430  0003 5f            	clrw	x
3431  0004 1f05          	ldw	(OFST-9,sp),x
3433                     ; 23     unsigned int numberOfSamples = 0;
3435  0006 5f            	clrw	x
3436  0007 1f0b          	ldw	(OFST-3,sp),x
3438                     ; 28     sumVSquared = 0;
3440  0009 ae0000        	ldw	x,#0
3441  000c bf9a          	ldw	_sumVSquared+2,x
3442  000e ae0000        	ldw	x,#0
3443  0011 bf98          	ldw	_sumVSquared,x
3444                     ; 29 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3446  0013 5f            	clrw	x
3447  0014 1f0d          	ldw	(OFST-1,sp),x
3449  0016               L7232:
3450                     ; 31 		sumISquared[i] = 0;
3452  0016 1e0d          	ldw	x,(OFST-1,sp)
3453  0018 58            	sllw	x
3454  0019 58            	sllw	x
3455  001a a600          	ld	a,#0
3456  001c e773          	ld	(_sumISquared+3,x),a
3457  001e a600          	ld	a,#0
3458  0020 e772          	ld	(_sumISquared+2,x),a
3459  0022 a600          	ld	a,#0
3460  0024 e771          	ld	(_sumISquared+1,x),a
3461  0026 a600          	ld	a,#0
3462  0028 e770          	ld	(_sumISquared,x),a
3463                     ; 32 		sumP[i] = 0;
3465  002a 1e0d          	ldw	x,(OFST-1,sp)
3466  002c 58            	sllw	x
3467  002d 58            	sllw	x
3468  002e a600          	ld	a,#0
3469  0030 e74f          	ld	(_sumP+3,x),a
3470  0032 a600          	ld	a,#0
3471  0034 e74e          	ld	(_sumP+2,x),a
3472  0036 a600          	ld	a,#0
3473  0038 e74d          	ld	(_sumP+1,x),a
3474  003a a600          	ld	a,#0
3475  003c e74c          	ld	(_sumP,x),a
3476                     ; 29 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3478  003e 1e0d          	ldw	x,(OFST-1,sp)
3479  0040 1c0001        	addw	x,#1
3480  0043 1f0d          	ldw	(OFST-1,sp),x
3484  0045 9c            	rvf
3485  0046 1e0d          	ldw	x,(OFST-1,sp)
3486  0048 a30009        	cpw	x,#9
3487  004b 2fc9          	jrslt	L7232
3488  004d               L5332:
3489                     ; 40 		startV = readChannel(VOLTAGE_CHANNEL);
3491  004d b600          	ld	a,_VOLTAGE_CHANNEL
3492  004f 5f            	clrw	x
3493  0050 97            	ld	xl,a
3494  0051 cd0000        	call	_readChannel
3496  0054 bfc0          	ldw	_startV,x
3497                     ; 42 	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45))));
3499  0056 9c            	rvf
3500  0057 bec0          	ldw	x,_startV
3501  0059 cd0000        	call	c_itof
3503  005c ae0010        	ldw	x,#L7432
3504  005f cd0000        	call	c_fcmp
3506  0062 2ee9          	jrsge	L5332
3508  0064 9c            	rvf
3509  0065 bec0          	ldw	x,_startV
3510  0067 cd0000        	call	c_itof
3512  006a ae000c        	ldw	x,#L7532
3513  006d cd0000        	call	c_fcmp
3515  0070 2ddb          	jrsle	L5332
3517  0072 acd101d1      	jpf	L5632
3518  0076               L3632:
3519                     ; 49         numberOfSamples++;
3521  0076 1e0b          	ldw	x,(OFST-3,sp)
3522  0078 1c0001        	addw	x,#1
3523  007b 1f0b          	ldw	(OFST-3,sp),x
3525                     ; 55         sampleV = readChannel(VOLTAGE_CHANNEL);
3527  007d b600          	ld	a,_VOLTAGE_CHANNEL
3528  007f 5f            	clrw	x
3529  0080 97            	ld	xl,a
3530  0081 cd0000        	call	_readChannel
3532  0084 bfbe          	ldw	_sampleV,x
3533                     ; 57 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3535  0086 5f            	clrw	x
3536  0087 1f0d          	ldw	(OFST-1,sp),x
3538  0089               L1732:
3539                     ; 58 			sampleI[i] = readChannel(CHANNELS[i]);
3541  0089 1e0d          	ldw	x,(OFST-1,sp)
3542  008b e600          	ld	a,(_CHANNELS,x)
3543  008d 5f            	clrw	x
3544  008e 97            	ld	xl,a
3545  008f cd0000        	call	_readChannel
3547  0092 160d          	ldw	y,(OFST-1,sp)
3548  0094 9058          	sllw	y
3549  0096 90efac        	ldw	(_sampleI,y),x
3550                     ; 57 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3552  0099 1e0d          	ldw	x,(OFST-1,sp)
3553  009b 1c0001        	addw	x,#1
3554  009e 1f0d          	ldw	(OFST-1,sp),x
3558  00a0 9c            	rvf
3559  00a1 1e0d          	ldw	x,(OFST-1,sp)
3560  00a3 a30009        	cpw	x,#9
3561  00a6 2fe1          	jrslt	L1732
3562                     ; 60         lastFilteredV = filteredV;
3564  00a8 bea6          	ldw	x,_filteredV+2
3565  00aa bfaa          	ldw	_lastFilteredV+2,x
3566  00ac bea4          	ldw	x,_filteredV
3567  00ae bfa8          	ldw	_lastFilteredV,x
3568                     ; 68         offsetV = offsetV + ((sampleV-offsetV)/1024);
3570  00b0 bebe          	ldw	x,_sampleV
3571  00b2 cd0000        	call	c_itof
3573  00b5 ae0000        	ldw	x,#_offsetV
3574  00b8 cd0000        	call	c_fsub
3576  00bb ae0008        	ldw	x,#L3042
3577  00be cd0000        	call	c_fdiv
3579  00c1 ae0000        	ldw	x,#_offsetV
3580  00c4 cd0000        	call	c_fgadd
3582                     ; 69         filteredV = sampleV - offsetV;
3584  00c7 bebe          	ldw	x,_sampleV
3585  00c9 cd0000        	call	c_itof
3587  00cc ae0000        	ldw	x,#_offsetV
3588  00cf cd0000        	call	c_fsub
3590  00d2 ae00a4        	ldw	x,#_filteredV
3591  00d5 cd0000        	call	c_rtol
3593                     ; 76         sumVSquared += filteredV * filteredV;
3595  00d8 ae00a4        	ldw	x,#_filteredV
3596  00db cd0000        	call	c_ltor
3598  00de ae00a4        	ldw	x,#_filteredV
3599  00e1 cd0000        	call	c_fmul
3601  00e4 ae0098        	ldw	x,#_sumVSquared
3602  00e7 cd0000        	call	c_fgadd
3604                     ; 81         phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
3606  00ea ae00a4        	ldw	x,#_filteredV
3607  00ed cd0000        	call	c_ltor
3609  00f0 ae00a8        	ldw	x,#_lastFilteredV
3610  00f3 cd0000        	call	c_fsub
3612  00f6 ae0004        	ldw	x,#L3142
3613  00f9 cd0000        	call	c_fmul
3615  00fc ae00a8        	ldw	x,#_lastFilteredV
3616  00ff cd0000        	call	c_fadd
3618  0102 ae009c        	ldw	x,#_phaseShiftedV
3619  0105 cd0000        	call	c_rtol
3621                     ; 83 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3623  0108 5f            	clrw	x
3624  0109 1f0d          	ldw	(OFST-1,sp),x
3626  010b               L7142:
3627                     ; 91 			offsetI = offsetI + ((sampleI[i]-offsetI)/1024);
3629  010b 1e0d          	ldw	x,(OFST-1,sp)
3630  010d 58            	sllw	x
3631  010e eeac          	ldw	x,(_sampleI,x)
3632  0110 cd0000        	call	c_itof
3634  0113 ae0004        	ldw	x,#_offsetI
3635  0116 cd0000        	call	c_fsub
3637  0119 ae0008        	ldw	x,#L3042
3638  011c cd0000        	call	c_fdiv
3640  011f ae0004        	ldw	x,#_offsetI
3641  0122 cd0000        	call	c_fgadd
3643                     ; 92 			filteredI = sampleI[i] - offsetI;
3645  0125 1e0d          	ldw	x,(OFST-1,sp)
3646  0127 58            	sllw	x
3647  0128 eeac          	ldw	x,(_sampleI,x)
3648  012a cd0000        	call	c_itof
3650  012d ae0004        	ldw	x,#_offsetI
3651  0130 cd0000        	call	c_fsub
3653  0133 ae00a0        	ldw	x,#_filteredI
3654  0136 cd0000        	call	c_rtol
3656                     ; 97 			sumISquared[i] += filteredI * filteredI;
3658  0139 ae00a0        	ldw	x,#_filteredI
3659  013c cd0000        	call	c_ltor
3661  013f ae00a0        	ldw	x,#_filteredI
3662  0142 cd0000        	call	c_fmul
3664  0145 1e0d          	ldw	x,(OFST-1,sp)
3665  0147 58            	sllw	x
3666  0148 58            	sllw	x
3667  0149 1c0070        	addw	x,#_sumISquared
3668  014c cd0000        	call	c_fgadd
3670                     ; 102 			instP = phaseShiftedV * filteredI;
3672  014f ae009c        	ldw	x,#_phaseShiftedV
3673  0152 cd0000        	call	c_ltor
3675  0155 ae00a0        	ldw	x,#_filteredI
3676  0158 cd0000        	call	c_fmul
3678  015b ae0094        	ldw	x,#_instP
3679  015e cd0000        	call	c_rtol
3681                     ; 103 			sumP[i] +=instP;
3683  0161 1e0d          	ldw	x,(OFST-1,sp)
3684  0163 58            	sllw	x
3685  0164 58            	sllw	x
3686  0165 b697          	ld	a,_instP+3
3687  0167 b703          	ld	c_lreg+3,a
3688  0169 b696          	ld	a,_instP+2
3689  016b b702          	ld	c_lreg+2,a
3690  016d b695          	ld	a,_instP+1
3691  016f b701          	ld	c_lreg+1,a
3692  0171 b694          	ld	a,_instP
3693  0173 b700          	ld	c_lreg,a
3694  0175 1c004c        	addw	x,#_sumP
3695  0178 cd0000        	call	c_fgadd
3697                     ; 83 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3699  017b 1e0d          	ldw	x,(OFST-1,sp)
3700  017d 1c0001        	addw	x,#1
3701  0180 1f0d          	ldw	(OFST-1,sp),x
3705  0182 9c            	rvf
3706  0183 1e0d          	ldw	x,(OFST-1,sp)
3707  0185 a30009        	cpw	x,#9
3708  0188 2e03          	jrsge	L61
3709  018a cc010b        	jp	L7142
3710  018d               L61:
3711                     ; 110         lastVCross = checkVCross;
3713                     	btst		_checkVCross
3714  0192 90110001      	bccm	_lastVCross
3715                     ; 111 		checkVCross = sampleV > startV;
3717  0196 9c            	rvf
3718  0197 bebe          	ldw	x,_sampleV
3719  0199 b3c0          	cpw	x,_startV
3720  019b 2c02          	jrsgt	L02
3721  019d 2006          	jp	L6
3722  019f               L02:
3723  019f 72100000      	bset	_checkVCross
3724  01a3 2004          	jra	L01
3725  01a5               L6:
3726  01a5 72110000      	bres	_checkVCross
3727  01a9               L01:
3728                     ; 112         if (numberOfSamples==1) lastVCross = checkVCross;
3730  01a9 1e0b          	ldw	x,(OFST-3,sp)
3731  01ab a30001        	cpw	x,#1
3732  01ae 2609          	jrne	L5242
3735                     	btst		_checkVCross
3736  01b5 90110001      	bccm	_lastVCross
3737  01b9               L5242:
3738                     ; 113         if (lastVCross != checkVCross) crossCount++;
3740  01b9 7201000107    	btjf	_lastVCross,L21
3741  01be 720000000e    	btjt	_checkVCross,L5632
3742  01c3 2005          	jra	L41
3743  01c5               L21:
3744  01c5 7201000007    	btjf	_checkVCross,L5632
3745  01ca               L41:
3748  01ca 1e05          	ldw	x,(OFST-9,sp)
3749  01cc 1c0001        	addw	x,#1
3750  01cf 1f05          	ldw	(OFST-9,sp),x
3752  01d1               L5632:
3753                     ; 47 	while(crossCount < crossings)
3755  01d1 1e05          	ldw	x,(OFST-9,sp)
3756  01d3 130f          	cpw	x,(OFST+1,sp)
3757  01d5 2403          	jruge	L22
3758  01d7 cc0076        	jp	L3632
3759  01da               L22:
3760                     ; 122 	VoltsPerCount = VCC / ADC_COUNTS; //constant 3.3/4096
3762  01da ce0002        	ldw	x,L5342+2
3763  01dd 1f09          	ldw	(OFST-5,sp),x
3764  01df ce0000        	ldw	x,L5342
3765  01e2 1f07          	ldw	(OFST-7,sp),x
3767                     ; 123     Vrms = VoltsPerCount * sqrt(sumVSquared / numberOfSamples);
3769  01e4 1e0b          	ldw	x,(OFST-3,sp)
3770  01e6 cd0000        	call	c_uitof
3772  01e9 96            	ldw	x,sp
3773  01ea 1c0001        	addw	x,#OFST-13
3774  01ed cd0000        	call	c_rtol
3777  01f0 ae0098        	ldw	x,#_sumVSquared
3778  01f3 cd0000        	call	c_ltor
3780  01f6 96            	ldw	x,sp
3781  01f7 1c0001        	addw	x,#OFST-13
3782  01fa cd0000        	call	c_fdiv
3784  01fd be02          	ldw	x,c_lreg+2
3785  01ff 89            	pushw	x
3786  0200 be00          	ldw	x,c_lreg
3787  0202 89            	pushw	x
3788  0203 cd0000        	call	_sqrt
3790  0206 5b04          	addw	sp,#4
3791  0208 96            	ldw	x,sp
3792  0209 1c0007        	addw	x,#OFST-7
3793  020c cd0000        	call	c_fmul
3795  020f ae0048        	ldw	x,#_Vrms
3796  0212 cd0000        	call	c_rtol
3798                     ; 125 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3800  0215 5f            	clrw	x
3801  0216 1f0d          	ldw	(OFST-1,sp),x
3803  0218               L1442:
3804                     ; 127 		Irms[i] = VoltsPerCount * sqrt(sumISquared[i] / numberOfSamples);
3806  0218 1e0b          	ldw	x,(OFST-3,sp)
3807  021a cd0000        	call	c_uitof
3809  021d 96            	ldw	x,sp
3810  021e 1c0001        	addw	x,#OFST-13
3811  0221 cd0000        	call	c_rtol
3814  0224 1e0d          	ldw	x,(OFST-1,sp)
3815  0226 58            	sllw	x
3816  0227 58            	sllw	x
3817  0228 1c0070        	addw	x,#_sumISquared
3818  022b cd0000        	call	c_ltor
3820  022e 96            	ldw	x,sp
3821  022f 1c0001        	addw	x,#OFST-13
3822  0232 cd0000        	call	c_fdiv
3824  0235 be02          	ldw	x,c_lreg+2
3825  0237 89            	pushw	x
3826  0238 be00          	ldw	x,c_lreg
3827  023a 89            	pushw	x
3828  023b cd0000        	call	_sqrt
3830  023e 5b04          	addw	sp,#4
3831  0240 96            	ldw	x,sp
3832  0241 1c0007        	addw	x,#OFST-7
3833  0244 cd0000        	call	c_fmul
3835  0247 1e0d          	ldw	x,(OFST-1,sp)
3836  0249 58            	sllw	x
3837  024a 58            	sllw	x
3838  024b 1c0000        	addw	x,#_Irms
3839  024e cd0000        	call	c_rtol
3841                     ; 129 		realPower[i] = VoltsPerCount * VoltsPerCount * sumP[i] / numberOfSamples;
3843  0251 1e0b          	ldw	x,(OFST-3,sp)
3844  0253 cd0000        	call	c_uitof
3846  0256 96            	ldw	x,sp
3847  0257 1c0001        	addw	x,#OFST-13
3848  025a cd0000        	call	c_rtol
3851  025d 96            	ldw	x,sp
3852  025e 1c0007        	addw	x,#OFST-7
3853  0261 cd0000        	call	c_ltor
3855  0264 96            	ldw	x,sp
3856  0265 1c0007        	addw	x,#OFST-7
3857  0268 cd0000        	call	c_fmul
3859  026b 1e0d          	ldw	x,(OFST-1,sp)
3860  026d 58            	sllw	x
3861  026e 58            	sllw	x
3862  026f 1c004c        	addw	x,#_sumP
3863  0272 cd0000        	call	c_fmul
3865  0275 96            	ldw	x,sp
3866  0276 1c0001        	addw	x,#OFST-13
3867  0279 cd0000        	call	c_fdiv
3869  027c 1e0d          	ldw	x,(OFST-1,sp)
3870  027e 58            	sllw	x
3871  027f 58            	sllw	x
3872  0280 1c0024        	addw	x,#_realPower
3873  0283 cd0000        	call	c_rtol
3875                     ; 125 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3877  0286 1e0d          	ldw	x,(OFST-1,sp)
3878  0288 1c0001        	addw	x,#1
3879  028b 1f0d          	ldw	(OFST-1,sp),x
3883  028d 9c            	rvf
3884  028e 1e0d          	ldw	x,(OFST-1,sp)
3885  0290 a30009        	cpw	x,#9
3886  0293 2e02          	jrsge	L42
3887  0295 2081          	jp	L1442
3888  0297               L42:
3889                     ; 132 }
3892  0297 5b10          	addw	sp,#16
3893  0299 81            	ret
3928                     ; 134 float getRealPower(unsigned int channelNo)
3928                     ; 135 {
3929                     	switch	.text
3930  029a               _getRealPower:
3934                     ; 136 	return realPower[channelNo];
3936  029a 58            	sllw	x
3937  029b 58            	sllw	x
3938  029c 1c0024        	addw	x,#_realPower
3939  029f cd0000        	call	c_ltor
3943  02a2 81            	ret
3978                     ; 139 float getIrms(unsigned int channelNo)
3978                     ; 140 {
3979                     	switch	.text
3980  02a3               _getIrms:
3984                     ; 141 	return Irms[channelNo];
3986  02a3 58            	sllw	x
3987  02a4 58            	sllw	x
3988  02a5 1c0000        	addw	x,#_Irms
3989  02a8 cd0000        	call	c_ltor
3993  02ab 81            	ret
4017                     ; 144 float getVrms()
4017                     ; 145 {
4018                     	switch	.text
4019  02ac               _getVrms:
4023                     ; 146 	return Vrms;
4025  02ac ae0048        	ldw	x,#_Vrms
4026  02af cd0000        	call	c_ltor
4030  02b2 81            	ret
4214                     	switch	.ubsct
4215  0000               _Irms:
4216  0000 000000000000  	ds.b	36
4217                     	xdef	_Irms
4218  0024               _realPower:
4219  0024 000000000000  	ds.b	36
4220                     	xdef	_realPower
4221  0048               _Vrms:
4222  0048 00000000      	ds.b	4
4223                     	xdef	_Vrms
4224                     .bit:	section	.data,bit
4225  0000               _checkVCross:
4226  0000 00            	ds.b	1
4227                     	xdef	_checkVCross
4228  0001               _lastVCross:
4229  0001 00            	ds.b	1
4230                     	xdef	_lastVCross
4231                     	switch	.ubsct
4232  004c               _sumP:
4233  004c 000000000000  	ds.b	36
4234                     	xdef	_sumP
4235  0070               _sumISquared:
4236  0070 000000000000  	ds.b	36
4237                     	xdef	_sumISquared
4238  0094               _instP:
4239  0094 00000000      	ds.b	4
4240                     	xdef	_instP
4241  0098               _sumVSquared:
4242  0098 00000000      	ds.b	4
4243                     	xdef	_sumVSquared
4244  009c               _phaseShiftedV:
4245  009c 00000000      	ds.b	4
4246                     	xdef	_phaseShiftedV
4247  00a0               _filteredI:
4248  00a0 00000000      	ds.b	4
4249                     	xdef	_filteredI
4250  00a4               _filteredV:
4251  00a4 00000000      	ds.b	4
4252                     	xdef	_filteredV
4253                     	xdef	_offsetI
4254                     	xdef	_offsetV
4255  00a8               _lastFilteredV:
4256  00a8 00000000      	ds.b	4
4257                     	xdef	_lastFilteredV
4258  00ac               _sampleI:
4259  00ac 000000000000  	ds.b	18
4260                     	xdef	_sampleI
4261  00be               _sampleV:
4262  00be 0000          	ds.b	2
4263                     	xdef	_sampleV
4264  00c0               _startV:
4265  00c0 0000          	ds.b	2
4266                     	xdef	_startV
4267                     	xdef	_getRealPower
4268                     	xdef	_getIrms
4269                     	xdef	_getVrms
4270                     	xdef	_calcVI
4271                     	xref	_readChannel
4272                     	xref.b	_VOLTAGE_CHANNEL
4273                     	xref.b	_CHANNELS
4274                     	xref	_sqrt
4275                     .const:	section	.text
4276  0000               L5342:
4277  0000 3a533333      	dc.w	14931,13107
4278  0004               L3142:
4279  0004 3fd99999      	dc.w	16345,-26215
4280  0008               L3042:
4281  0008 44800000      	dc.w	17536,0
4282  000c               L7532:
4283  000c 44e66666      	dc.w	17638,26214
4284  0010               L7432:
4285  0010 450ccccc      	dc.w	17676,-13108
4286                     	xref.b	c_lreg
4287                     	xref.b	c_x
4307                     	xref	c_uitof
4308                     	xref	c_fadd
4309                     	xref	c_fmul
4310                     	xref	c_ltor
4311                     	xref	c_rtol
4312                     	xref	c_fgadd
4313                     	xref	c_fdiv
4314                     	xref	c_fsub
4315                     	xref	c_fcmp
4316                     	xref	c_itof
4317                     	end
