   1                     ; C Compiler for STM8 (COSMIC Software)
   2                     ; Parser V4.10.2 - 02 Nov 2011
   3                     ; Generator (Limited) V4.3.7 - 29 Nov 2011
3317                     .const:	section	.text
3318  0000               _ADC_COUNTS:
3319  0000 1000          	dc.w	4096
3320  0002               _PHASECAL:
3321  0002               L7622:
3322  0002 3fd99999      	dc.w	16345,-26215
3468                     ; 24 void calcVI(char vPin, char iPin, unsigned int crossings)
3468                     ; 25 {
3470                     	switch	.text
3471  0000               _calcVI:
3473  0000 89            	pushw	x
3474  0001 5212          	subw	sp,#18
3475       00000012      OFST:	set	18
3478                     ; 26 	int SupplyVoltage=3300;
3480  0003 ae0ce4        	ldw	x,#3300
3481  0006 1f05          	ldw	(OFST-13,sp),x
3482                     ; 27   unsigned int crossCount = 0;
3484  0008 5f            	clrw	x
3485  0009 1f0f          	ldw	(OFST-3,sp),x
3486                     ; 28   unsigned int numberOfSamples = 0;
3488  000b 5f            	clrw	x
3489  000c 1f11          	ldw	(OFST-1,sp),x
3490                     ; 31 	float VCAL = 1;
3492  000e a601          	ld	a,#1
3493  0010 cd0000        	call	c_ctof
3495  0013 96            	ldw	x,sp
3496  0014 1c0007        	addw	x,#OFST-11
3497  0017 cd0000        	call	c_rtol
3499                     ; 32 	float ICAL = 1;
3501  001a a601          	ld	a,#1
3502  001c cd0000        	call	c_ctof
3504  001f 96            	ldw	x,sp
3505  0020 1c000b        	addw	x,#OFST-7
3506  0023 cd0000        	call	c_rtol
3508  0026               L3632:
3509                     ; 37     startV = readChannel(vPin);
3511  0026 7b13          	ld	a,(OFST+1,sp)
3512  0028 5f            	clrw	x
3513  0029 97            	ld	xl,a
3514  002a cd0000        	call	_readChannel
3516  002d bf00          	ldw	_startV,x
3517                     ; 38     if((startV<(ADC_COUNTS*0.55))&&(startV>(ADC_COUNTS*0.45)))break;
3519  002f 9c            	rvf
3520  0030 be00          	ldw	x,_startV
3521  0032 cd0000        	call	c_itof
3523  0035 ae001a        	ldw	x,#L5732
3524  0038 cd0000        	call	c_fcmp
3526  003b 2ee9          	jrsge	L3632
3528  003d 9c            	rvf
3529  003e be00          	ldw	x,_startV
3530  0040 cd0000        	call	c_itof
3532  0043 ae0016        	ldw	x,#L5042
3533  0046 cd0000        	call	c_fcmp
3535  0049 2ddb          	jrsle	L3632
3538  004b ac7a017a      	jpf	L3142
3539  004f               L1142:
3540                     ; 44     numberOfSamples++;                       //Count number of times looped.
3542  004f 1e11          	ldw	x,(OFST-1,sp)
3543  0051 1c0001        	addw	x,#1
3544  0054 1f11          	ldw	(OFST-1,sp),x
3545                     ; 45     lastFilteredV = filteredV;               //Used for delay/phase compensation
3547  0056 be2c          	ldw	x,_filteredV+2
3548  0058 bf30          	ldw	_lastFilteredV+2,x
3549  005a be2a          	ldw	x,_filteredV
3550  005c bf2e          	ldw	_lastFilteredV,x
3551                     ; 50     sampleV = readChannel(vPin);                 //Read in raw voltage signal
3553  005e 7b13          	ld	a,(OFST+1,sp)
3554  0060 5f            	clrw	x
3555  0061 97            	ld	xl,a
3556  0062 cd0000        	call	_readChannel
3558  0065 bf3c          	ldw	_sampleV,x
3559                     ; 51     sampleI = readChannel(iPin);                 //Read in raw current signal
3561  0067 7b14          	ld	a,(OFST+2,sp)
3562  0069 5f            	clrw	x
3563  006a 97            	ld	xl,a
3564  006b cd0000        	call	_readChannel
3566  006e bf3a          	ldw	_sampleI,x
3567                     ; 57     offsetV = offsetV + ((sampleV-offsetV)/1024);
3569  0070 be3c          	ldw	x,_sampleV
3570  0072 cd0000        	call	c_itof
3572  0075 ae0022        	ldw	x,#_offsetV
3573  0078 cd0000        	call	c_fsub
3575  007b ae0012        	ldw	x,#L3242
3576  007e cd0000        	call	c_fdiv
3578  0081 ae0022        	ldw	x,#_offsetV
3579  0084 cd0000        	call	c_fgadd
3581                     ; 58     filteredV = sampleV - offsetV;
3583  0087 be3c          	ldw	x,_sampleV
3584  0089 cd0000        	call	c_itof
3586  008c ae0022        	ldw	x,#_offsetV
3587  008f cd0000        	call	c_fsub
3589  0092 ae002a        	ldw	x,#_filteredV
3590  0095 cd0000        	call	c_rtol
3592                     ; 59     offsetI = offsetI + ((sampleI-offsetI)/1024);
3594  0098 be3a          	ldw	x,_sampleI
3595  009a cd0000        	call	c_itof
3597  009d ae001e        	ldw	x,#_offsetI
3598  00a0 cd0000        	call	c_fsub
3600  00a3 ae0012        	ldw	x,#L3242
3601  00a6 cd0000        	call	c_fdiv
3603  00a9 ae001e        	ldw	x,#_offsetI
3604  00ac cd0000        	call	c_fgadd
3606                     ; 60     filteredI = sampleI - offsetI;
3608  00af be3a          	ldw	x,_sampleI
3609  00b1 cd0000        	call	c_itof
3611  00b4 ae001e        	ldw	x,#_offsetI
3612  00b7 cd0000        	call	c_fsub
3614  00ba ae0026        	ldw	x,#_filteredI
3615  00bd cd0000        	call	c_rtol
3617                     ; 65     sqV= filteredV * filteredV;                 //1) square voltage values
3619  00c0 ae002a        	ldw	x,#_filteredV
3620  00c3 cd0000        	call	c_ltor
3622  00c6 ae002a        	ldw	x,#_filteredV
3623  00c9 cd0000        	call	c_fmul
3625  00cc ae0016        	ldw	x,#_sqV
3626  00cf cd0000        	call	c_rtol
3628                     ; 66     sumV += sqV;                                //2) sum
3630  00d2 ae0016        	ldw	x,#_sqV
3631  00d5 cd0000        	call	c_ltor
3633  00d8 ae0012        	ldw	x,#_sumV
3634  00db cd0000        	call	c_fgadd
3636                     ; 71     sqI = filteredI * filteredI;                //1) square current values
3638  00de ae0026        	ldw	x,#_filteredI
3639  00e1 cd0000        	call	c_ltor
3641  00e4 ae0026        	ldw	x,#_filteredI
3642  00e7 cd0000        	call	c_fmul
3644  00ea ae000e        	ldw	x,#_sqI
3645  00ed cd0000        	call	c_rtol
3647                     ; 72     sumI += sqI;                                //2) sum
3649  00f0 ae000e        	ldw	x,#_sqI
3650  00f3 cd0000        	call	c_ltor
3652  00f6 ae000a        	ldw	x,#_sumI
3653  00f9 cd0000        	call	c_fgadd
3655                     ; 77     phaseShiftedV = lastFilteredV + PHASECAL * (filteredV - lastFilteredV);
3657  00fc ae002a        	ldw	x,#_filteredV
3658  00ff cd0000        	call	c_ltor
3660  0102 ae002e        	ldw	x,#_lastFilteredV
3661  0105 cd0000        	call	c_fsub
3663  0108 ae0002        	ldw	x,#L7622
3664  010b cd0000        	call	c_fmul
3666  010e ae002e        	ldw	x,#_lastFilteredV
3667  0111 cd0000        	call	c_fadd
3669  0114 ae001a        	ldw	x,#_phaseShiftedV
3670  0117 cd0000        	call	c_rtol
3672                     ; 82     instP = phaseShiftedV * filteredI;          //Instantaneous Power
3674  011a ae001a        	ldw	x,#_phaseShiftedV
3675  011d cd0000        	call	c_ltor
3677  0120 ae0026        	ldw	x,#_filteredI
3678  0123 cd0000        	call	c_fmul
3680  0126 ae0006        	ldw	x,#_instP
3681  0129 cd0000        	call	c_rtol
3683                     ; 83     sumP +=instP;                               //Sum
3685  012c ae0006        	ldw	x,#_instP
3686  012f cd0000        	call	c_ltor
3688  0132 ae0002        	ldw	x,#_sumP
3689  0135 cd0000        	call	c_fgadd
3691                     ; 90     lastVCross = checkVCross;
3693                     	btst		_checkVCross
3694  013d 90110001      	bccm	_lastVCross
3695                     ; 91     if (sampleV > startV) checkVCross = true;
3697  0141 9c            	rvf
3698  0142 be3c          	ldw	x,_sampleV
3699  0144 b300          	cpw	x,_startV
3700  0146 2d06          	jrsle	L7242
3703  0148 72100000      	bset	_checkVCross
3705  014c 2004          	jra	L1342
3706  014e               L7242:
3707                     ; 92                      else checkVCross = false;
3709  014e 72110000      	bres	_checkVCross
3710  0152               L1342:
3711                     ; 93     if (numberOfSamples==1) lastVCross = checkVCross;
3713  0152 1e11          	ldw	x,(OFST-1,sp)
3714  0154 a30001        	cpw	x,#1
3715  0157 2609          	jrne	L3342
3718                     	btst		_checkVCross
3719  015e 90110001      	bccm	_lastVCross
3720  0162               L3342:
3721                     ; 95     if (lastVCross != checkVCross) crossCount++;
3723  0162 7201000107    	btjf	_lastVCross,L6
3724  0167 720000000e    	btjt	_checkVCross,L3142
3725  016c 2005          	jra	L01
3726  016e               L6:
3727  016e 7201000007    	btjf	_checkVCross,L3142
3728  0173               L01:
3731  0173 1e0f          	ldw	x,(OFST-3,sp)
3732  0175 1c0001        	addw	x,#1
3733  0178 1f0f          	ldw	(OFST-3,sp),x
3734  017a               L3142:
3735                     ; 41 	while(crossCount < crossings)
3737  017a 1e0f          	ldw	x,(OFST-3,sp)
3738  017c 1317          	cpw	x,(OFST+5,sp)
3739  017e 2403          	jruge	L21
3740  0180 cc004f        	jp	L1142
3741  0183               L21:
3742                     ; 105   V_RATIO = VCAL *((SupplyVoltage/1000.0) / (ADC_COUNTS));
3744  0183 1e05          	ldw	x,(OFST-13,sp)
3745  0185 cd0000        	call	c_itof
3747  0188 ae000e        	ldw	x,#L3442
3748  018b cd0000        	call	c_fdiv
3750  018e ae000a        	ldw	x,#L3542
3751  0191 cd0000        	call	c_fdiv
3753  0194 96            	ldw	x,sp
3754  0195 1c0007        	addw	x,#OFST-11
3755  0198 cd0000        	call	c_fmul
3757  019b 96            	ldw	x,sp
3758  019c 1c0007        	addw	x,#OFST-11
3759  019f cd0000        	call	c_rtol
3761                     ; 106   Vrms = V_RATIO * root(sumV / numberOfSamples);
3763  01a2 1e11          	ldw	x,(OFST-1,sp)
3764  01a4 cd0000        	call	c_uitof
3766  01a7 96            	ldw	x,sp
3767  01a8 1c0001        	addw	x,#OFST-17
3768  01ab cd0000        	call	c_rtol
3770  01ae ae0012        	ldw	x,#_sumV
3771  01b1 cd0000        	call	c_ltor
3773  01b4 96            	ldw	x,sp
3774  01b5 1c0001        	addw	x,#OFST-17
3775  01b8 cd0000        	call	c_fdiv
3777  01bb be02          	ldw	x,c_lreg+2
3778  01bd 89            	pushw	x
3779  01be be00          	ldw	x,c_lreg
3780  01c0 89            	pushw	x
3781  01c1 cd0290        	call	_root
3783  01c4 5b04          	addw	sp,#4
3784  01c6 96            	ldw	x,sp
3785  01c7 1c0007        	addw	x,#OFST-11
3786  01ca cd0000        	call	c_fmul
3788  01cd ae0042        	ldw	x,#_Vrms
3789  01d0 cd0000        	call	c_rtol
3791                     ; 108   I_RATIO = ICAL *((SupplyVoltage/1000.0) / (ADC_COUNTS));
3793  01d3 1e05          	ldw	x,(OFST-13,sp)
3794  01d5 cd0000        	call	c_itof
3796  01d8 ae000e        	ldw	x,#L3442
3797  01db cd0000        	call	c_fdiv
3799  01de ae000a        	ldw	x,#L3542
3800  01e1 cd0000        	call	c_fdiv
3802  01e4 96            	ldw	x,sp
3803  01e5 1c000b        	addw	x,#OFST-7
3804  01e8 cd0000        	call	c_fmul
3806  01eb 96            	ldw	x,sp
3807  01ec 1c000b        	addw	x,#OFST-7
3808  01ef cd0000        	call	c_rtol
3810                     ; 109   Irms = I_RATIO * root(sumI / numberOfSamples);
3812  01f2 1e11          	ldw	x,(OFST-1,sp)
3813  01f4 cd0000        	call	c_uitof
3815  01f7 96            	ldw	x,sp
3816  01f8 1c0001        	addw	x,#OFST-17
3817  01fb cd0000        	call	c_rtol
3819  01fe ae000a        	ldw	x,#_sumI
3820  0201 cd0000        	call	c_ltor
3822  0204 96            	ldw	x,sp
3823  0205 1c0001        	addw	x,#OFST-17
3824  0208 cd0000        	call	c_fdiv
3826  020b be02          	ldw	x,c_lreg+2
3827  020d 89            	pushw	x
3828  020e be00          	ldw	x,c_lreg
3829  0210 89            	pushw	x
3830  0211 ad7d          	call	_root
3832  0213 5b04          	addw	sp,#4
3833  0215 96            	ldw	x,sp
3834  0216 1c000b        	addw	x,#OFST-7
3835  0219 cd0000        	call	c_fmul
3837  021c ae003e        	ldw	x,#_Irms
3838  021f cd0000        	call	c_rtol
3840                     ; 112   realPower = V_RATIO * I_RATIO * sumP / numberOfSamples;
3842  0222 1e11          	ldw	x,(OFST-1,sp)
3843  0224 cd0000        	call	c_uitof
3845  0227 96            	ldw	x,sp
3846  0228 1c0001        	addw	x,#OFST-17
3847  022b cd0000        	call	c_rtol
3849  022e 96            	ldw	x,sp
3850  022f 1c0007        	addw	x,#OFST-11
3851  0232 cd0000        	call	c_ltor
3853  0235 96            	ldw	x,sp
3854  0236 1c000b        	addw	x,#OFST-7
3855  0239 cd0000        	call	c_fmul
3857  023c ae0002        	ldw	x,#_sumP
3858  023f cd0000        	call	c_fmul
3860  0242 96            	ldw	x,sp
3861  0243 1c0001        	addw	x,#OFST-17
3862  0246 cd0000        	call	c_fdiv
3864  0249 ae004e        	ldw	x,#_realPower
3865  024c cd0000        	call	c_rtol
3867                     ; 113   apparentPower = Vrms * Irms;
3869  024f ae0042        	ldw	x,#_Vrms
3870  0252 cd0000        	call	c_ltor
3872  0255 ae003e        	ldw	x,#_Irms
3873  0258 cd0000        	call	c_fmul
3875  025b ae004a        	ldw	x,#_apparentPower
3876  025e cd0000        	call	c_rtol
3878                     ; 117   sumV = 0;
3880  0261 ae0000        	ldw	x,#0
3881  0264 bf14          	ldw	_sumV+2,x
3882  0266 ae0000        	ldw	x,#0
3883  0269 bf12          	ldw	_sumV,x
3884                     ; 118   sumI = 0;
3886  026b ae0000        	ldw	x,#0
3887  026e bf0c          	ldw	_sumI+2,x
3888  0270 ae0000        	ldw	x,#0
3889  0273 bf0a          	ldw	_sumI,x
3890                     ; 119   sumP = 0;
3892  0275 ae0000        	ldw	x,#0
3893  0278 bf04          	ldw	_sumP+2,x
3894  027a ae0000        	ldw	x,#0
3895  027d bf02          	ldw	_sumP,x
3896                     ; 120 }
3899  027f 5b14          	addw	sp,#20
3900  0281 81            	ret
3924                     ; 122 double getRealPower()
3924                     ; 123 {
3925                     	switch	.text
3926  0282               _getRealPower:
3930                     ; 124 	return realPower;
3932  0282 ae004e        	ldw	x,#_realPower
3933  0285 cd0000        	call	c_ltor
3937  0288 81            	ret
3961                     ; 127 double getApparentPower()
3961                     ; 128 {
3962                     	switch	.text
3963  0289               _getApparentPower:
3967                     ; 129 	return apparentPower;
3969  0289 ae004a        	ldw	x,#_apparentPower
3970  028c cd0000        	call	c_ltor
3974  028f 81            	ret
4044                     ; 132 double root(double n)
4044                     ; 133 {
4045                     	switch	.text
4046  0290               _root:
4048  0290 520e          	subw	sp,#14
4049       0000000e      OFST:	set	14
4052                     ; 134   double lo = 0, hi = n, mid;
4054  0292 ae0000        	ldw	x,#0
4055  0295 1f03          	ldw	(OFST-11,sp),x
4056  0297 ae0000        	ldw	x,#0
4057  029a 1f01          	ldw	(OFST-13,sp),x
4060  029c 1e13          	ldw	x,(OFST+5,sp)
4061  029e 1f07          	ldw	(OFST-7,sp),x
4062  02a0 1e11          	ldw	x,(OFST+3,sp)
4063  02a2 1f05          	ldw	(OFST-9,sp),x
4064                     ; 135 	int i = 0;
4066                     ; 136   for(i = 0 ; i < 1000 ; i++){
4068  02a4 5f            	clrw	x
4069  02a5 1f09          	ldw	(OFST-5,sp),x
4070  02a7               L5352:
4071                     ; 137       mid = (lo+hi)/2;
4073  02a7 96            	ldw	x,sp
4074  02a8 1c0001        	addw	x,#OFST-13
4075  02ab cd0000        	call	c_ltor
4077  02ae 96            	ldw	x,sp
4078  02af 1c0005        	addw	x,#OFST-9
4079  02b2 cd0000        	call	c_fadd
4081  02b5 ae0006        	ldw	x,#L7452
4082  02b8 cd0000        	call	c_fdiv
4084  02bb 96            	ldw	x,sp
4085  02bc 1c000b        	addw	x,#OFST-3
4086  02bf cd0000        	call	c_rtol
4088                     ; 138       if(mid*mid == n) return mid;
4090  02c2 96            	ldw	x,sp
4091  02c3 1c000b        	addw	x,#OFST-3
4092  02c6 cd0000        	call	c_ltor
4094  02c9 96            	ldw	x,sp
4095  02ca 1c000b        	addw	x,#OFST-3
4096  02cd cd0000        	call	c_fmul
4098  02d0 96            	ldw	x,sp
4099  02d1 1c0011        	addw	x,#OFST+3
4100  02d4 cd0000        	call	c_fcmp
4102  02d7 2609          	jrne	L3552
4105  02d9 96            	ldw	x,sp
4106  02da 1c000b        	addw	x,#OFST-3
4107  02dd cd0000        	call	c_ltor
4110  02e0 2040          	jra	L22
4111  02e2               L3552:
4112                     ; 139       if(mid*mid > n){
4114  02e2 9c            	rvf
4115  02e3 96            	ldw	x,sp
4116  02e4 1c000b        	addw	x,#OFST-3
4117  02e7 cd0000        	call	c_ltor
4119  02ea 96            	ldw	x,sp
4120  02eb 1c000b        	addw	x,#OFST-3
4121  02ee cd0000        	call	c_fmul
4123  02f1 96            	ldw	x,sp
4124  02f2 1c0011        	addw	x,#OFST+3
4125  02f5 cd0000        	call	c_fcmp
4127  02f8 2d0a          	jrsle	L5552
4128                     ; 140           hi = mid;
4130  02fa 1e0d          	ldw	x,(OFST-1,sp)
4131  02fc 1f07          	ldw	(OFST-7,sp),x
4132  02fe 1e0b          	ldw	x,(OFST-3,sp)
4133  0300 1f05          	ldw	(OFST-9,sp),x
4135  0302 2008          	jra	L7552
4136  0304               L5552:
4137                     ; 142           lo = mid;
4139  0304 1e0d          	ldw	x,(OFST-1,sp)
4140  0306 1f03          	ldw	(OFST-11,sp),x
4141  0308 1e0b          	ldw	x,(OFST-3,sp)
4142  030a 1f01          	ldw	(OFST-13,sp),x
4143  030c               L7552:
4144                     ; 136   for(i = 0 ; i < 1000 ; i++){
4146  030c 1e09          	ldw	x,(OFST-5,sp)
4147  030e 1c0001        	addw	x,#1
4148  0311 1f09          	ldw	(OFST-5,sp),x
4151  0313 9c            	rvf
4152  0314 1e09          	ldw	x,(OFST-5,sp)
4153  0316 a303e8        	cpw	x,#1000
4154  0319 2f8c          	jrslt	L5352
4155                     ; 145   return mid;
4157  031b 96            	ldw	x,sp
4158  031c 1c000b        	addw	x,#OFST-3
4159  031f cd0000        	call	c_ltor
4162  0322               L22:
4164  0322 5b0e          	addw	sp,#14
4165  0324 81            	ret
4416                     .bit:	section	.data,bit
4417  0000               _checkVCross:
4418  0000 00            	ds.b	1
4419                     	xdef	_checkVCross
4420  0001               _lastVCross:
4421  0001 00            	ds.b	1
4422                     	xdef	_lastVCross
4423                     	switch	.ubsct
4424  0000               _startV:
4425  0000 0000          	ds.b	2
4426                     	xdef	_startV
4427  0002               _sumP:
4428  0002 00000000      	ds.b	4
4429                     	xdef	_sumP
4430  0006               _instP:
4431  0006 00000000      	ds.b	4
4432                     	xdef	_instP
4433  000a               _sumI:
4434  000a 00000000      	ds.b	4
4435                     	xdef	_sumI
4436  000e               _sqI:
4437  000e 00000000      	ds.b	4
4438                     	xdef	_sqI
4439  0012               _sumV:
4440  0012 00000000      	ds.b	4
4441                     	xdef	_sumV
4442  0016               _sqV:
4443  0016 00000000      	ds.b	4
4444                     	xdef	_sqV
4445  001a               _phaseShiftedV:
4446  001a 00000000      	ds.b	4
4447                     	xdef	_phaseShiftedV
4448  001e               _offsetI:
4449  001e 00000000      	ds.b	4
4450                     	xdef	_offsetI
4451  0022               _offsetV:
4452  0022 00000000      	ds.b	4
4453                     	xdef	_offsetV
4454  0026               _filteredI:
4455  0026 00000000      	ds.b	4
4456                     	xdef	_filteredI
4457  002a               _filteredV:
4458  002a 00000000      	ds.b	4
4459                     	xdef	_filteredV
4460  002e               _lastFilteredV:
4461  002e 00000000      	ds.b	4
4462                     	xdef	_lastFilteredV
4463  0032               _ICAL:
4464  0032 00000000      	ds.b	4
4465                     	xdef	_ICAL
4466  0036               _VCAL:
4467  0036 00000000      	ds.b	4
4468                     	xdef	_VCAL
4469  003a               _sampleI:
4470  003a 0000          	ds.b	2
4471                     	xdef	_sampleI
4472  003c               _sampleV:
4473  003c 0000          	ds.b	2
4474                     	xdef	_sampleV
4475  003e               _Irms:
4476  003e 00000000      	ds.b	4
4477                     	xdef	_Irms
4478  0042               _Vrms:
4479  0042 00000000      	ds.b	4
4480                     	xdef	_Vrms
4481  0046               _powerFactor:
4482  0046 00000000      	ds.b	4
4483                     	xdef	_powerFactor
4484  004a               _apparentPower:
4485  004a 00000000      	ds.b	4
4486                     	xdef	_apparentPower
4487  004e               _realPower:
4488  004e 00000000      	ds.b	4
4489                     	xdef	_realPower
4490                     	xdef	_root
4491                     	xdef	_getRealPower
4492                     	xdef	_getApparentPower
4493                     	xdef	_calcVI
4494                     	xdef	_PHASECAL
4495                     	xdef	_ADC_COUNTS
4496                     	xref	_readChannel
4497                     	switch	.const
4498  0006               L7452:
4499  0006 40000000      	dc.w	16384,0
4500  000a               L3542:
4501  000a 45800000      	dc.w	17792,0
4502  000e               L3442:
4503  000e 447a0000      	dc.w	17530,0
4504  0012               L3242:
4505  0012 44800000      	dc.w	17536,0
4506  0016               L5042:
4507  0016 44e66666      	dc.w	17638,26214
4508  001a               L5732:
4509  001a 450ccccc      	dc.w	17676,-13108
4510                     	xref.b	c_lreg
4511                     	xref.b	c_x
4531                     	xref	c_uitof
4532                     	xref	c_fadd
4533                     	xref	c_fmul
4534                     	xref	c_ltor
4535                     	xref	c_fgadd
4536                     	xref	c_fdiv
4537                     	xref	c_fsub
4538                     	xref	c_fcmp
4539                     	xref	c_itof
4540                     	xref	c_rtol
4541                     	xref	c_ctof
4542                     	end
