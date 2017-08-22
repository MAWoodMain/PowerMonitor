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
3781  01c1 cd0298        	call	_root
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
3830  0211 cd0298        	call	_root
3832  0214 5b04          	addw	sp,#4
3833  0216 96            	ldw	x,sp
3834  0217 1c000b        	addw	x,#OFST-7
3835  021a cd0000        	call	c_fmul
3837  021d ae003e        	ldw	x,#_Irms
3838  0220 cd0000        	call	c_rtol
3840                     ; 112   realPower = V_RATIO * I_RATIO * sumP / numberOfSamples;
3842  0223 1e11          	ldw	x,(OFST-1,sp)
3843  0225 cd0000        	call	c_uitof
3845  0228 96            	ldw	x,sp
3846  0229 1c0001        	addw	x,#OFST-17
3847  022c cd0000        	call	c_rtol
3849  022f 96            	ldw	x,sp
3850  0230 1c0007        	addw	x,#OFST-11
3851  0233 cd0000        	call	c_ltor
3853  0236 96            	ldw	x,sp
3854  0237 1c000b        	addw	x,#OFST-7
3855  023a cd0000        	call	c_fmul
3857  023d ae0002        	ldw	x,#_sumP
3858  0240 cd0000        	call	c_fmul
3860  0243 96            	ldw	x,sp
3861  0244 1c0001        	addw	x,#OFST-17
3862  0247 cd0000        	call	c_fdiv
3864  024a ae004e        	ldw	x,#_realPower
3865  024d cd0000        	call	c_rtol
3867                     ; 113   apparentPower = Vrms * Irms;
3869  0250 ae0042        	ldw	x,#_Vrms
3870  0253 cd0000        	call	c_ltor
3872  0256 ae003e        	ldw	x,#_Irms
3873  0259 cd0000        	call	c_fmul
3875  025c ae004a        	ldw	x,#_apparentPower
3876  025f cd0000        	call	c_rtol
3878                     ; 117   sumV = 0;
3880  0262 ae0000        	ldw	x,#0
3881  0265 bf14          	ldw	_sumV+2,x
3882  0267 ae0000        	ldw	x,#0
3883  026a bf12          	ldw	_sumV,x
3884                     ; 118   sumI = 0;
3886  026c ae0000        	ldw	x,#0
3887  026f bf0c          	ldw	_sumI+2,x
3888  0271 ae0000        	ldw	x,#0
3889  0274 bf0a          	ldw	_sumI,x
3890                     ; 119   sumP = 0;
3892  0276 ae0000        	ldw	x,#0
3893  0279 bf04          	ldw	_sumP+2,x
3894  027b ae0000        	ldw	x,#0
3895  027e bf02          	ldw	_sumP,x
3896                     ; 120 }
3899  0280 5b14          	addw	sp,#20
3900  0282 81            	ret
3924                     ; 122 double getRealPower()
3924                     ; 123 {
3925                     	switch	.text
3926  0283               _getRealPower:
3930                     ; 124 	return realPower;
3932  0283 ae004e        	ldw	x,#_realPower
3933  0286 cd0000        	call	c_ltor
3937  0289 81            	ret
3961                     ; 127 double getApparentPower()
3961                     ; 128 {
3962                     	switch	.text
3963  028a               _getApparentPower:
3967                     ; 129 	return apparentPower;
3969  028a ae004a        	ldw	x,#_apparentPower
3970  028d cd0000        	call	c_ltor
3974  0290 81            	ret
3998                     ; 132 double getVrms()
3998                     ; 133 {
3999                     	switch	.text
4000  0291               _getVrms:
4004                     ; 134 return Vrms;
4006  0291 ae0042        	ldw	x,#_Vrms
4007  0294 cd0000        	call	c_ltor
4011  0297 81            	ret
4081                     ; 137 double root(double n)
4081                     ; 138 {
4082                     	switch	.text
4083  0298               _root:
4085  0298 520e          	subw	sp,#14
4086       0000000e      OFST:	set	14
4089                     ; 139   double lo = 0, hi = n, mid;
4091  029a ae0000        	ldw	x,#0
4092  029d 1f03          	ldw	(OFST-11,sp),x
4093  029f ae0000        	ldw	x,#0
4094  02a2 1f01          	ldw	(OFST-13,sp),x
4097  02a4 1e13          	ldw	x,(OFST+5,sp)
4098  02a6 1f07          	ldw	(OFST-7,sp),x
4099  02a8 1e11          	ldw	x,(OFST+3,sp)
4100  02aa 1f05          	ldw	(OFST-9,sp),x
4101                     ; 140 	int i = 0;
4103                     ; 141   for(i = 0 ; i < 1000 ; i++){
4105  02ac 5f            	clrw	x
4106  02ad 1f09          	ldw	(OFST-5,sp),x
4107  02af               L5452:
4108                     ; 142       mid = (lo+hi)/2;
4110  02af 96            	ldw	x,sp
4111  02b0 1c0001        	addw	x,#OFST-13
4112  02b3 cd0000        	call	c_ltor
4114  02b6 96            	ldw	x,sp
4115  02b7 1c0005        	addw	x,#OFST-9
4116  02ba cd0000        	call	c_fadd
4118  02bd ae0006        	ldw	x,#L7552
4119  02c0 cd0000        	call	c_fdiv
4121  02c3 96            	ldw	x,sp
4122  02c4 1c000b        	addw	x,#OFST-3
4123  02c7 cd0000        	call	c_rtol
4125                     ; 143       if(mid*mid == n) return mid;
4127  02ca 96            	ldw	x,sp
4128  02cb 1c000b        	addw	x,#OFST-3
4129  02ce cd0000        	call	c_ltor
4131  02d1 96            	ldw	x,sp
4132  02d2 1c000b        	addw	x,#OFST-3
4133  02d5 cd0000        	call	c_fmul
4135  02d8 96            	ldw	x,sp
4136  02d9 1c0011        	addw	x,#OFST+3
4137  02dc cd0000        	call	c_fcmp
4139  02df 2609          	jrne	L3652
4142  02e1 96            	ldw	x,sp
4143  02e2 1c000b        	addw	x,#OFST-3
4144  02e5 cd0000        	call	c_ltor
4147  02e8 2040          	jra	L42
4148  02ea               L3652:
4149                     ; 144       if(mid*mid > n){
4151  02ea 9c            	rvf
4152  02eb 96            	ldw	x,sp
4153  02ec 1c000b        	addw	x,#OFST-3
4154  02ef cd0000        	call	c_ltor
4156  02f2 96            	ldw	x,sp
4157  02f3 1c000b        	addw	x,#OFST-3
4158  02f6 cd0000        	call	c_fmul
4160  02f9 96            	ldw	x,sp
4161  02fa 1c0011        	addw	x,#OFST+3
4162  02fd cd0000        	call	c_fcmp
4164  0300 2d0a          	jrsle	L5652
4165                     ; 145           hi = mid;
4167  0302 1e0d          	ldw	x,(OFST-1,sp)
4168  0304 1f07          	ldw	(OFST-7,sp),x
4169  0306 1e0b          	ldw	x,(OFST-3,sp)
4170  0308 1f05          	ldw	(OFST-9,sp),x
4172  030a 2008          	jra	L7652
4173  030c               L5652:
4174                     ; 147           lo = mid;
4176  030c 1e0d          	ldw	x,(OFST-1,sp)
4177  030e 1f03          	ldw	(OFST-11,sp),x
4178  0310 1e0b          	ldw	x,(OFST-3,sp)
4179  0312 1f01          	ldw	(OFST-13,sp),x
4180  0314               L7652:
4181                     ; 141   for(i = 0 ; i < 1000 ; i++){
4183  0314 1e09          	ldw	x,(OFST-5,sp)
4184  0316 1c0001        	addw	x,#1
4185  0319 1f09          	ldw	(OFST-5,sp),x
4188  031b 9c            	rvf
4189  031c 1e09          	ldw	x,(OFST-5,sp)
4190  031e a303e8        	cpw	x,#1000
4191  0321 2f8c          	jrslt	L5452
4192                     ; 150   return mid;
4194  0323 96            	ldw	x,sp
4195  0324 1c000b        	addw	x,#OFST-3
4196  0327 cd0000        	call	c_ltor
4199  032a               L42:
4201  032a 5b0e          	addw	sp,#14
4202  032c 81            	ret
4453                     .bit:	section	.data,bit
4454  0000               _checkVCross:
4455  0000 00            	ds.b	1
4456                     	xdef	_checkVCross
4457  0001               _lastVCross:
4458  0001 00            	ds.b	1
4459                     	xdef	_lastVCross
4460                     	switch	.ubsct
4461  0000               _startV:
4462  0000 0000          	ds.b	2
4463                     	xdef	_startV
4464  0002               _sumP:
4465  0002 00000000      	ds.b	4
4466                     	xdef	_sumP
4467  0006               _instP:
4468  0006 00000000      	ds.b	4
4469                     	xdef	_instP
4470  000a               _sumI:
4471  000a 00000000      	ds.b	4
4472                     	xdef	_sumI
4473  000e               _sqI:
4474  000e 00000000      	ds.b	4
4475                     	xdef	_sqI
4476  0012               _sumV:
4477  0012 00000000      	ds.b	4
4478                     	xdef	_sumV
4479  0016               _sqV:
4480  0016 00000000      	ds.b	4
4481                     	xdef	_sqV
4482  001a               _phaseShiftedV:
4483  001a 00000000      	ds.b	4
4484                     	xdef	_phaseShiftedV
4485  001e               _offsetI:
4486  001e 00000000      	ds.b	4
4487                     	xdef	_offsetI
4488  0022               _offsetV:
4489  0022 00000000      	ds.b	4
4490                     	xdef	_offsetV
4491  0026               _filteredI:
4492  0026 00000000      	ds.b	4
4493                     	xdef	_filteredI
4494  002a               _filteredV:
4495  002a 00000000      	ds.b	4
4496                     	xdef	_filteredV
4497  002e               _lastFilteredV:
4498  002e 00000000      	ds.b	4
4499                     	xdef	_lastFilteredV
4500  0032               _ICAL:
4501  0032 00000000      	ds.b	4
4502                     	xdef	_ICAL
4503  0036               _VCAL:
4504  0036 00000000      	ds.b	4
4505                     	xdef	_VCAL
4506  003a               _sampleI:
4507  003a 0000          	ds.b	2
4508                     	xdef	_sampleI
4509  003c               _sampleV:
4510  003c 0000          	ds.b	2
4511                     	xdef	_sampleV
4512  003e               _Irms:
4513  003e 00000000      	ds.b	4
4514                     	xdef	_Irms
4515  0042               _Vrms:
4516  0042 00000000      	ds.b	4
4517                     	xdef	_Vrms
4518  0046               _powerFactor:
4519  0046 00000000      	ds.b	4
4520                     	xdef	_powerFactor
4521  004a               _apparentPower:
4522  004a 00000000      	ds.b	4
4523                     	xdef	_apparentPower
4524  004e               _realPower:
4525  004e 00000000      	ds.b	4
4526                     	xdef	_realPower
4527                     	xdef	_root
4528                     	xdef	_getVrms
4529                     	xdef	_getRealPower
4530                     	xdef	_getApparentPower
4531                     	xdef	_calcVI
4532                     	xdef	_PHASECAL
4533                     	xdef	_ADC_COUNTS
4534                     	xref	_readChannel
4535                     	switch	.const
4536  0006               L7552:
4537  0006 40000000      	dc.w	16384,0
4538  000a               L3542:
4539  000a 45800000      	dc.w	17792,0
4540  000e               L3442:
4541  000e 447a0000      	dc.w	17530,0
4542  0012               L3242:
4543  0012 44800000      	dc.w	17536,0
4544  0016               L5042:
4545  0016 44e66666      	dc.w	17638,26214
4546  001a               L5732:
4547  001a 450ccccc      	dc.w	17676,-13108
4548                     	xref.b	c_lreg
4549                     	xref.b	c_x
4569                     	xref	c_uitof
4570                     	xref	c_fadd
4571                     	xref	c_fmul
4572                     	xref	c_ltor
4573                     	xref	c_fgadd
4574                     	xref	c_fdiv
4575                     	xref	c_fsub
4576                     	xref	c_fcmp
4577                     	xref	c_itof
4578                     	xref	c_rtol
4579                     	xref	c_ctof
4580                     	end
