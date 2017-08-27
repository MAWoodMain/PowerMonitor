   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3413                     ; 15 void calcVI(const unsigned int crossings)
3413                     ; 16 {
3415                     	switch	.text
3416  0000               _calcVI:
3418  0000 89            	pushw	x
3419  0001 520e          	subw	sp,#14
3420       0000000e      OFST:	set	14
3423                     ; 17     unsigned int crossCount = 0;
3425  0003 5f            	clrw	x
3426  0004 1f05          	ldw	(OFST-9,sp),x
3427                     ; 18     unsigned int numberOfSamples = 0;
3429  0006 5f            	clrw	x
3430  0007 1f0b          	ldw	(OFST-3,sp),x
3431                     ; 23     sumVSquared = 0;
3433  0009 ae0000        	ldw	x,#0
3434  000c bf9a          	ldw	_sumVSquared+2,x
3435  000e ae0000        	ldw	x,#0
3436  0011 bf98          	ldw	_sumVSquared,x
3437                     ; 24 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3439  0013 5f            	clrw	x
3440  0014 1f0d          	ldw	(OFST-1,sp),x
3441  0016               L7232:
3442                     ; 26 		sumISquared[i] = 0;
3444  0016 1e0d          	ldw	x,(OFST-1,sp)
3445  0018 58            	sllw	x
3446  0019 58            	sllw	x
3447  001a a600          	ld	a,#0
3448  001c e773          	ld	(_sumISquared+3,x),a
3449  001e a600          	ld	a,#0
3450  0020 e772          	ld	(_sumISquared+2,x),a
3451  0022 a600          	ld	a,#0
3452  0024 e771          	ld	(_sumISquared+1,x),a
3453  0026 a600          	ld	a,#0
3454  0028 e770          	ld	(_sumISquared,x),a
3455                     ; 27 		sumP[i] = 0;
3457  002a 1e0d          	ldw	x,(OFST-1,sp)
3458  002c 58            	sllw	x
3459  002d 58            	sllw	x
3460  002e a600          	ld	a,#0
3461  0030 e74f          	ld	(_sumP+3,x),a
3462  0032 a600          	ld	a,#0
3463  0034 e74e          	ld	(_sumP+2,x),a
3464  0036 a600          	ld	a,#0
3465  0038 e74d          	ld	(_sumP+1,x),a
3466  003a a600          	ld	a,#0
3467  003c e74c          	ld	(_sumP,x),a
3468                     ; 24 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3470  003e 1e0d          	ldw	x,(OFST-1,sp)
3471  0040 1c0001        	addw	x,#1
3472  0043 1f0d          	ldw	(OFST-1,sp),x
3475  0045 9c            	rvf
3476  0046 1e0d          	ldw	x,(OFST-1,sp)
3477  0048 a30009        	cpw	x,#9
3478  004b 2fc9          	jrslt	L7232
3479  004d               L5332:
3480                     ; 32 		startV = readChannel(VOLTAGE_CHANNEL);
3482  004d b600          	ld	a,_VOLTAGE_CHANNEL
3483  004f 5f            	clrw	x
3484  0050 97            	ld	xl,a
3485  0051 cd0000        	call	_readChannel
3487  0054 bfc8          	ldw	_startV,x
3488                     ; 34 	} while (!((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))); 
3490  0056 9c            	rvf
3491  0057 bec8          	ldw	x,_startV
3492  0059 cd0000        	call	c_itof
3494  005c ae0010        	ldw	x,#L7432
3495  005f cd0000        	call	c_fcmp
3497  0062 2ee9          	jrsge	L5332
3499  0064 9c            	rvf
3500  0065 bec8          	ldw	x,_startV
3501  0067 cd0000        	call	c_itof
3503  006a ae000c        	ldw	x,#L7532
3504  006d cd0000        	call	c_fcmp
3506  0070 2ddb          	jrsle	L5332
3508  0072 acd101d1      	jpf	L5632
3509  0076               L3632:
3510                     ; 38         numberOfSamples++;
3512  0076 1e0b          	ldw	x,(OFST-3,sp)
3513  0078 1c0001        	addw	x,#1
3514  007b 1f0b          	ldw	(OFST-3,sp),x
3515                     ; 41         sampleV = readChannel(VOLTAGE_CHANNEL);
3517  007d b600          	ld	a,_VOLTAGE_CHANNEL
3518  007f 5f            	clrw	x
3519  0080 97            	ld	xl,a
3520  0081 cd0000        	call	_readChannel
3522  0084 bfc6          	ldw	_sampleV,x
3523                     ; 43 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3525  0086 5f            	clrw	x
3526  0087 1f0d          	ldw	(OFST-1,sp),x
3527  0089               L1732:
3528                     ; 44 			sampleI[i] = readChannel(CHANNELS[i]);
3530  0089 1e0d          	ldw	x,(OFST-1,sp)
3531  008b e600          	ld	a,(_CHANNELS,x)
3532  008d 5f            	clrw	x
3533  008e 97            	ld	xl,a
3534  008f cd0000        	call	_readChannel
3536  0092 160d          	ldw	y,(OFST-1,sp)
3537  0094 9058          	sllw	y
3538  0096 90efb4        	ldw	(_sampleI,y),x
3539                     ; 43 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3541  0099 1e0d          	ldw	x,(OFST-1,sp)
3542  009b 1c0001        	addw	x,#1
3543  009e 1f0d          	ldw	(OFST-1,sp),x
3546  00a0 9c            	rvf
3547  00a1 1e0d          	ldw	x,(OFST-1,sp)
3548  00a3 a30009        	cpw	x,#9
3549  00a6 2fe1          	jrslt	L1732
3550                     ; 46         lastOffsetV = offsetV;
3552  00a8 beae          	ldw	x,_offsetV+2
3553  00aa bfb2          	ldw	_lastOffsetV+2,x
3554  00ac beac          	ldw	x,_offsetV
3555  00ae bfb0          	ldw	_lastOffsetV,x
3556                     ; 48         vOffset = vOffset + ((sampleV-vOffset)/1024);
3558  00b0 bec6          	ldw	x,_sampleV
3559  00b2 cd0000        	call	c_itof
3561  00b5 ae00a4        	ldw	x,#_vOffset
3562  00b8 cd0000        	call	c_fsub
3564  00bb ae0008        	ldw	x,#L3042
3565  00be cd0000        	call	c_fdiv
3567  00c1 ae00a4        	ldw	x,#_vOffset
3568  00c4 cd0000        	call	c_fgadd
3570                     ; 49         offsetV = sampleV - vOffset;
3572  00c7 bec6          	ldw	x,_sampleV
3573  00c9 cd0000        	call	c_itof
3575  00cc ae00a4        	ldw	x,#_vOffset
3576  00cf cd0000        	call	c_fsub
3578  00d2 ae00ac        	ldw	x,#_offsetV
3579  00d5 cd0000        	call	c_rtol
3581                     ; 51         sumVSquared += offsetV * offsetV;
3583  00d8 ae00ac        	ldw	x,#_offsetV
3584  00db cd0000        	call	c_ltor
3586  00de ae00ac        	ldw	x,#_offsetV
3587  00e1 cd0000        	call	c_fmul
3589  00e4 ae0098        	ldw	x,#_sumVSquared
3590  00e7 cd0000        	call	c_fgadd
3592                     ; 53         phaseShiftedV = lastOffsetV + PHASECAL * (offsetV - lastOffsetV); //??????
3594  00ea ae00ac        	ldw	x,#_offsetV
3595  00ed cd0000        	call	c_ltor
3597  00f0 ae00b0        	ldw	x,#_lastOffsetV
3598  00f3 cd0000        	call	c_fsub
3600  00f6 ae0004        	ldw	x,#L3142
3601  00f9 cd0000        	call	c_fmul
3603  00fc ae00b0        	ldw	x,#_lastOffsetV
3604  00ff cd0000        	call	c_fadd
3606  0102 ae009c        	ldw	x,#_phaseShiftedV
3607  0105 cd0000        	call	c_rtol
3609                     ; 55 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3611  0108 5f            	clrw	x
3612  0109 1f0d          	ldw	(OFST-1,sp),x
3613  010b               L7142:
3614                     ; 58 			iOffset = iOffset + ((sampleI[i]-iOffset)/1024);
3616  010b 1e0d          	ldw	x,(OFST-1,sp)
3617  010d 58            	sllw	x
3618  010e eeb4          	ldw	x,(_sampleI,x)
3619  0110 cd0000        	call	c_itof
3621  0113 ae00a0        	ldw	x,#_iOffset
3622  0116 cd0000        	call	c_fsub
3624  0119 ae0008        	ldw	x,#L3042
3625  011c cd0000        	call	c_fdiv
3627  011f ae00a0        	ldw	x,#_iOffset
3628  0122 cd0000        	call	c_fgadd
3630                     ; 59 			offsetI = sampleI[i] - iOffset;
3632  0125 1e0d          	ldw	x,(OFST-1,sp)
3633  0127 58            	sllw	x
3634  0128 eeb4          	ldw	x,(_sampleI,x)
3635  012a cd0000        	call	c_itof
3637  012d ae00a0        	ldw	x,#_iOffset
3638  0130 cd0000        	call	c_fsub
3640  0133 ae00a8        	ldw	x,#_offsetI
3641  0136 cd0000        	call	c_rtol
3643                     ; 60 			sumISquared[i] += offsetI * offsetI;
3645  0139 ae00a8        	ldw	x,#_offsetI
3646  013c cd0000        	call	c_ltor
3648  013f ae00a8        	ldw	x,#_offsetI
3649  0142 cd0000        	call	c_fmul
3651  0145 1e0d          	ldw	x,(OFST-1,sp)
3652  0147 58            	sllw	x
3653  0148 58            	sllw	x
3654  0149 1c0070        	addw	x,#_sumISquared
3655  014c cd0000        	call	c_fgadd
3657                     ; 62 			instP = phaseShiftedV * offsetI; //??????
3659  014f ae009c        	ldw	x,#_phaseShiftedV
3660  0152 cd0000        	call	c_ltor
3662  0155 ae00a8        	ldw	x,#_offsetI
3663  0158 cd0000        	call	c_fmul
3665  015b ae0094        	ldw	x,#_instP
3666  015e cd0000        	call	c_rtol
3668                     ; 63 			sumP[i] +=instP;
3670  0161 1e0d          	ldw	x,(OFST-1,sp)
3671  0163 58            	sllw	x
3672  0164 58            	sllw	x
3673  0165 b697          	ld	a,_instP+3
3674  0167 b703          	ld	c_lreg+3,a
3675  0169 b696          	ld	a,_instP+2
3676  016b b702          	ld	c_lreg+2,a
3677  016d b695          	ld	a,_instP+1
3678  016f b701          	ld	c_lreg+1,a
3679  0171 b694          	ld	a,_instP
3680  0173 b700          	ld	c_lreg,a
3681  0175 1c004c        	addw	x,#_sumP
3682  0178 cd0000        	call	c_fgadd
3684                     ; 55 		for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3686  017b 1e0d          	ldw	x,(OFST-1,sp)
3687  017d 1c0001        	addw	x,#1
3688  0180 1f0d          	ldw	(OFST-1,sp),x
3691  0182 9c            	rvf
3692  0183 1e0d          	ldw	x,(OFST-1,sp)
3693  0185 a30009        	cpw	x,#9
3694  0188 2e03          	jrsge	L61
3695  018a cc010b        	jp	L7142
3696  018d               L61:
3697                     ; 66         lastVCross = checkVCross;
3699                     	btst		_checkVCross
3700  0192 90110001      	bccm	_lastVCross
3701                     ; 67 		checkVCross = sampleV > startV;
3703  0196 9c            	rvf
3704  0197 bec6          	ldw	x,_sampleV
3705  0199 b3c8          	cpw	x,_startV
3706  019b 2c02          	jrsgt	L02
3707  019d 2006          	jp	L6
3708  019f               L02:
3709  019f 72100000      	bset	_checkVCross
3710  01a3 2004          	jra	L01
3711  01a5               L6:
3712  01a5 72110000      	bres	_checkVCross
3713  01a9               L01:
3714                     ; 68         if (numberOfSamples==1) lastVCross = checkVCross;
3716  01a9 1e0b          	ldw	x,(OFST-3,sp)
3717  01ab a30001        	cpw	x,#1
3718  01ae 2609          	jrne	L5242
3721                     	btst		_checkVCross
3722  01b5 90110001      	bccm	_lastVCross
3723  01b9               L5242:
3724                     ; 69         if (lastVCross != checkVCross) crossCount++;
3726  01b9 7201000107    	btjf	_lastVCross,L21
3727  01be 720000000e    	btjt	_checkVCross,L5632
3728  01c3 2005          	jra	L41
3729  01c5               L21:
3730  01c5 7201000007    	btjf	_checkVCross,L5632
3731  01ca               L41:
3734  01ca 1e05          	ldw	x,(OFST-9,sp)
3735  01cc 1c0001        	addw	x,#1
3736  01cf 1f05          	ldw	(OFST-9,sp),x
3737  01d1               L5632:
3738                     ; 36 	while(crossCount < crossings)
3740  01d1 1e05          	ldw	x,(OFST-9,sp)
3741  01d3 130f          	cpw	x,(OFST+1,sp)
3742  01d5 2403          	jruge	L22
3743  01d7 cc0076        	jp	L3632
3744  01da               L22:
3745                     ; 72 	VoltsPerCount = VCC / ADC_COUNTS; //constant 3.3/4096
3747  01da ce0002        	ldw	x,L5342+2
3748  01dd 1f09          	ldw	(OFST-5,sp),x
3749  01df ce0000        	ldw	x,L5342
3750  01e2 1f07          	ldw	(OFST-7,sp),x
3751                     ; 73     Vrms = VoltsPerCount * sqrt(sumVSquared / numberOfSamples);
3753  01e4 1e0b          	ldw	x,(OFST-3,sp)
3754  01e6 cd0000        	call	c_uitof
3756  01e9 96            	ldw	x,sp
3757  01ea 1c0001        	addw	x,#OFST-13
3758  01ed cd0000        	call	c_rtol
3760  01f0 ae0098        	ldw	x,#_sumVSquared
3761  01f3 cd0000        	call	c_ltor
3763  01f6 96            	ldw	x,sp
3764  01f7 1c0001        	addw	x,#OFST-13
3765  01fa cd0000        	call	c_fdiv
3767  01fd be02          	ldw	x,c_lreg+2
3768  01ff 89            	pushw	x
3769  0200 be00          	ldw	x,c_lreg
3770  0202 89            	pushw	x
3771  0203 cd0000        	call	_sqrt
3773  0206 5b04          	addw	sp,#4
3774  0208 96            	ldw	x,sp
3775  0209 1c0007        	addw	x,#OFST-7
3776  020c cd0000        	call	c_fmul
3778  020f ae0048        	ldw	x,#_Vrms
3779  0212 cd0000        	call	c_rtol
3781                     ; 75 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3783  0215 5f            	clrw	x
3784  0216 1f0d          	ldw	(OFST-1,sp),x
3785  0218               L1442:
3786                     ; 77 		Irms[i] = VoltsPerCount * sqrt(sumISquared[i] / numberOfSamples);
3788  0218 1e0b          	ldw	x,(OFST-3,sp)
3789  021a cd0000        	call	c_uitof
3791  021d 96            	ldw	x,sp
3792  021e 1c0001        	addw	x,#OFST-13
3793  0221 cd0000        	call	c_rtol
3795  0224 1e0d          	ldw	x,(OFST-1,sp)
3796  0226 58            	sllw	x
3797  0227 58            	sllw	x
3798  0228 1c0070        	addw	x,#_sumISquared
3799  022b cd0000        	call	c_ltor
3801  022e 96            	ldw	x,sp
3802  022f 1c0001        	addw	x,#OFST-13
3803  0232 cd0000        	call	c_fdiv
3805  0235 be02          	ldw	x,c_lreg+2
3806  0237 89            	pushw	x
3807  0238 be00          	ldw	x,c_lreg
3808  023a 89            	pushw	x
3809  023b cd0000        	call	_sqrt
3811  023e 5b04          	addw	sp,#4
3812  0240 96            	ldw	x,sp
3813  0241 1c0007        	addw	x,#OFST-7
3814  0244 cd0000        	call	c_fmul
3816  0247 1e0d          	ldw	x,(OFST-1,sp)
3817  0249 58            	sllw	x
3818  024a 58            	sllw	x
3819  024b 1c0000        	addw	x,#_Irms
3820  024e cd0000        	call	c_rtol
3822                     ; 79 		realPower[i] = VoltsPerCount * VoltsPerCount * sumP[i] / numberOfSamples;
3824  0251 1e0b          	ldw	x,(OFST-3,sp)
3825  0253 cd0000        	call	c_uitof
3827  0256 96            	ldw	x,sp
3828  0257 1c0001        	addw	x,#OFST-13
3829  025a cd0000        	call	c_rtol
3831  025d 96            	ldw	x,sp
3832  025e 1c0007        	addw	x,#OFST-7
3833  0261 cd0000        	call	c_ltor
3835  0264 96            	ldw	x,sp
3836  0265 1c0007        	addw	x,#OFST-7
3837  0268 cd0000        	call	c_fmul
3839  026b 1e0d          	ldw	x,(OFST-1,sp)
3840  026d 58            	sllw	x
3841  026e 58            	sllw	x
3842  026f 1c004c        	addw	x,#_sumP
3843  0272 cd0000        	call	c_fmul
3845  0275 96            	ldw	x,sp
3846  0276 1c0001        	addw	x,#OFST-13
3847  0279 cd0000        	call	c_fdiv
3849  027c 1e0d          	ldw	x,(OFST-1,sp)
3850  027e 58            	sllw	x
3851  027f 58            	sllw	x
3852  0280 1c0024        	addw	x,#_realPower
3853  0283 cd0000        	call	c_rtol
3855                     ; 75 	for(i = 0; i < HARDWARE_CHANNEL_NUM; i++)
3857  0286 1e0d          	ldw	x,(OFST-1,sp)
3858  0288 1c0001        	addw	x,#1
3859  028b 1f0d          	ldw	(OFST-1,sp),x
3862  028d 9c            	rvf
3863  028e 1e0d          	ldw	x,(OFST-1,sp)
3864  0290 a30009        	cpw	x,#9
3865  0293 2e02          	jrsge	L42
3866  0295 2081          	jp	L1442
3867  0297               L42:
3868                     ; 81 }
3871  0297 5b10          	addw	sp,#16
3872  0299 81            	ret
3907                     ; 83 float getRealPower(unsigned int channelNo)
3907                     ; 84 {
3908                     	switch	.text
3909  029a               _getRealPower:
3913                     ; 85 	return realPower[channelNo];
3915  029a 58            	sllw	x
3916  029b 58            	sllw	x
3917  029c 1c0024        	addw	x,#_realPower
3918  029f cd0000        	call	c_ltor
3922  02a2 81            	ret
3957                     ; 88 float getIrms(unsigned int channelNo)
3957                     ; 89 {
3958                     	switch	.text
3959  02a3               _getIrms:
3963                     ; 90 	return Irms[channelNo];
3965  02a3 58            	sllw	x
3966  02a4 58            	sllw	x
3967  02a5 1c0000        	addw	x,#_Irms
3968  02a8 cd0000        	call	c_ltor
3972  02ab 81            	ret
3996                     ; 93 float getVrms()
3996                     ; 94 {
3997                     	switch	.text
3998  02ac               _getVrms:
4002                     ; 95 	return Vrms;
4004  02ac ae0048        	ldw	x,#_Vrms
4005  02af cd0000        	call	c_ltor
4009  02b2 81            	ret
4193                     	switch	.ubsct
4194  0000               _Irms:
4195  0000 000000000000  	ds.b	36
4196                     	xdef	_Irms
4197  0024               _realPower:
4198  0024 000000000000  	ds.b	36
4199                     	xdef	_realPower
4200  0048               _Vrms:
4201  0048 00000000      	ds.b	4
4202                     	xdef	_Vrms
4203                     .bit:	section	.data,bit
4204  0000               _checkVCross:
4205  0000 00            	ds.b	1
4206                     	xdef	_checkVCross
4207  0001               _lastVCross:
4208  0001 00            	ds.b	1
4209                     	xdef	_lastVCross
4210                     	switch	.ubsct
4211  004c               _sumP:
4212  004c 000000000000  	ds.b	36
4213                     	xdef	_sumP
4214  0070               _sumISquared:
4215  0070 000000000000  	ds.b	36
4216                     	xdef	_sumISquared
4217  0094               _instP:
4218  0094 00000000      	ds.b	4
4219                     	xdef	_instP
4220  0098               _sumVSquared:
4221  0098 00000000      	ds.b	4
4222                     	xdef	_sumVSquared
4223  009c               _phaseShiftedV:
4224  009c 00000000      	ds.b	4
4225                     	xdef	_phaseShiftedV
4226  00a0               _iOffset:
4227  00a0 00000000      	ds.b	4
4228                     	xdef	_iOffset
4229  00a4               _vOffset:
4230  00a4 00000000      	ds.b	4
4231                     	xdef	_vOffset
4232  00a8               _offsetI:
4233  00a8 00000000      	ds.b	4
4234                     	xdef	_offsetI
4235  00ac               _offsetV:
4236  00ac 00000000      	ds.b	4
4237                     	xdef	_offsetV
4238  00b0               _lastOffsetV:
4239  00b0 00000000      	ds.b	4
4240                     	xdef	_lastOffsetV
4241  00b4               _sampleI:
4242  00b4 000000000000  	ds.b	18
4243                     	xdef	_sampleI
4244  00c6               _sampleV:
4245  00c6 0000          	ds.b	2
4246                     	xdef	_sampleV
4247  00c8               _startV:
4248  00c8 0000          	ds.b	2
4249                     	xdef	_startV
4250                     	xdef	_getRealPower
4251                     	xdef	_getIrms
4252                     	xdef	_getVrms
4253                     	xdef	_calcVI
4254                     	xref	_readChannel
4255                     	xref.b	_VOLTAGE_CHANNEL
4256                     	xref.b	_CHANNELS
4257                     	xref	_sqrt
4258                     .const:	section	.text
4259  0000               L5342:
4260  0000 3a533333      	dc.w	14931,13107
4261  0004               L3142:
4262  0004 3fd99999      	dc.w	16345,-26215
4263  0008               L3042:
4264  0008 44800000      	dc.w	17536,0
4265  000c               L7532:
4266  000c 44e66666      	dc.w	17638,26214
4267  0010               L7432:
4268  0010 450ccccc      	dc.w	17676,-13108
4269                     	xref.b	c_lreg
4270                     	xref.b	c_x
4290                     	xref	c_uitof
4291                     	xref	c_fadd
4292                     	xref	c_fmul
4293                     	xref	c_ltor
4294                     	xref	c_rtol
4295                     	xref	c_fgadd
4296                     	xref	c_fdiv
4297                     	xref	c_fsub
4298                     	xref	c_fcmp
4299                     	xref	c_itof
4300                     	end
