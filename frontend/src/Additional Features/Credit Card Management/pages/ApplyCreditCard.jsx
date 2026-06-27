import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { creditCardService } from '../services/creditCardsService';
import {
  User, Phone, CreditCard, CheckCircle, ArrowLeft, ShieldCheck, Star,
  FileSignature, KeyRound, Building2, Upload, Camera, FileText, RefreshCw,
} from 'lucide-react';
import toast from 'react-hot-toast';

const CONSENT_OPTIONS = [
  { value: 'PHYSICAL_FORM', label: 'Physical Form' },
  { value: 'DIGITAL_SIGNATURE', label: 'Digital Signature' },
  { value: 'VERBAL_OTP', label: 'Verbal / OTP' },
];

const LEAD_SOURCES = [
  { value: 'BRANCH', label: 'Branch Walk-in' },
  { value: 'COLD_CALL', label: 'Cold Call' },
  { value: 'DIGITAL_CAMPAIGN', label: 'Digital Campaign' },
];

const ApplyCreditCard = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const [form, setForm] = useState({
    // Applicant
    cardHolderName: '',
    userId: '',
    mobileNumber: '',
    vendor: 'VISA',
    cardType: '',
    // Consent
    consentSources: [],
    // Metadata
    leadSource: '',
    sourcingBranchCode: '',
    // Documents
    kycDocumentName: '',
    incomeDocumentName: '',
  });

  // --- OTP state ---------------------------------------------------------
  const [generatedOtp, setGeneratedOtp] = useState('');
  const [otpInput, setOtpInput] = useState('');
  const [otpVerified, setOtpVerified] = useState(false);

  // --- Live photo state --------------------------------------------------
  const [photoCaptured, setPhotoCaptured] = useState(false);
  const [cameraOn, setCameraOn] = useState(false);
  const [photoUrl, setPhotoUrl] = useState('');
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);

  const set = (field, value) => setForm((f) => ({ ...f, [field]: value }));

  // ---- OTP handlers -----------------------------------------------------
  const sendOtp = () => {
    if (!/^\d{10}$/.test(form.mobileNumber)) {
      toast.error('Enter a valid 10-digit applicant mobile number first');
      return;
    }
    const otp = String(Math.floor(100000 + Math.random() * 900000));
    setGeneratedOtp(otp);
    setOtpVerified(false);
    setOtpInput('');
    // Demo: surface the OTP that would have been SMS-ed to the applicant.
    toast.success(`OTP sent to ${form.mobileNumber} (demo: ${otp})`, { duration: 6000 });
  };

  const verifyOtp = () => {
    if (otpInput === generatedOtp && generatedOtp !== '') {
      setOtpVerified(true);
      toast.success('Applicant OTP verified');
    } else {
      toast.error('Incorrect OTP');
    }
  };

  // ---- Webcam handlers --------------------------------------------------
  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true });
      streamRef.current = stream;
      setCameraOn(true);
      // attach after the <video> renders
      setTimeout(() => {
        if (videoRef.current) videoRef.current.srcObject = stream;
      }, 0);
    } catch (e) {
      toast.error('Webcam unavailable. Use a Video-KYC link instead.');
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
    }
    setCameraOn(false);
  };

  const capturePhoto = () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas) return;
    canvas.width = video.videoWidth || 320;
    canvas.height = video.videoHeight || 240;
    canvas.getContext('2d').drawImage(video, 0, 0, canvas.width, canvas.height);
    setPhotoUrl(canvas.toDataURL('image/jpeg', 0.8));
    setPhotoCaptured(true);
    stopCamera();
    toast.success('Live photograph captured');
  };

  // cleanup camera on unmount
  useEffect(() => () => stopCamera(), []);

  // ---- Validation -------------------------------------------------------
  const validate = () => {
    const e = {};
    if (!form.cardHolderName || form.cardHolderName.trim().length < 2) e.cardHolderName = 'Applicant name is required';
    if (!form.userId || !/^\d+$/.test(String(form.userId))) e.userId = 'Valid applicant user ID is required';
    if (!/^\d{10}$/.test(form.mobileNumber)) e.mobileNumber = '10-digit mobile number is required';
    if (!form.cardType) e.cardType = 'Select a card category';
    if (form.consentSources.length === 0) e.consentSources = 'Record at least one consent source';
    if (!otpVerified) e.otp = 'Applicant OTP must be verified';
    if (!form.leadSource) e.leadSource = 'Lead source is required';
    if (!form.sourcingBranchCode) e.sourcingBranchCode = 'Sourcing branch code is required';
    if (!form.kycDocumentName) e.kycDocumentName = 'Upload the KYC document scan';
    if (!form.incomeDocumentName) e.incomeDocumentName = 'Upload the income document scan';
    if (!photoCaptured) e.photo = 'Capture a live photograph / video-KYC';
    return e;
  };

  const errors = submitted ? validate() : {};
  const toggleConsent = (value) => {
    setForm((f) => ({
      ...f,
      consentSources: f.consentSources.includes(value)
        ? f.consentSources.filter((c) => c !== value)
        : [...f.consentSources, value],
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitted(true);
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      toast.error('Please complete all required application fields');
      return;
    }
    try {
      setLoading(true);
      const res = await creditCardService.applyForCard({
        ...form,
        otpVerified,
        applicantPhotoCaptured: photoCaptured,
      });
      if (res.success) {
        toast.success('Application Submitted Successfully!');
        navigate('/credit-cards');
      }
    } catch (err) {
      toast.error(err.message || 'Application Failed');
    } finally {
      setLoading(false);
    }
  };

  const cardOptions = [
    { type: 'Silver', features: ['Zero joining fee', '1% Cashback'] },
    { type: 'Gold', features: ['Lounge access', '2% Cashback'] },
    { type: 'Platinum', features: ['Premium rewards', 'Travel insurance'] },
  ];

  const inputClass = (field) =>
    `pl-11 block w-full rounded-xl shadow-sm sm:text-sm py-3 border transition-all ${
      errors[field]
        ? 'border-red-300 bg-red-50 focus:ring-red-500 focus:border-red-500'
        : 'border-gray-200 bg-gray-50 focus:bg-white focus:ring-2 focus:ring-primary-500 focus:border-primary-500'
    }`;

  const SectionHeader = ({ icon: Icon, title, sub }) => (
    <div className="flex items-center gap-2 border-b border-gray-100 pb-2">
      <Icon className="w-5 h-5 text-primary-600" />
      <h2 className="text-lg font-semibold text-gray-800">{title}</h2>
      {sub && <span className="text-xs text-gray-400 ml-1">{sub}</span>}
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      <div className="flex items-center gap-4 mb-2">
        <button onClick={() => navigate('/credit-cards')} className="p-2.5 bg-white shadow-sm hover:bg-gray-50 rounded-full border border-gray-100">
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">New Credit Card Application</h1>
          <p className="text-gray-500 mt-1">Officer-assisted onboarding — capture applicant consent, KYC and sourcing details.</p>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
        <div className="p-8 sm:p-10">
          <form onSubmit={handleSubmit} className="space-y-10">

            {/* Section 1 — Applicant Details */}
            <div className="space-y-6">
              <SectionHeader icon={User} title="1. Applicant Details" />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Applicant Name *</label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input className={inputClass('cardHolderName')} placeholder="Full name as per KYC"
                      value={form.cardHolderName} onChange={(e) => set('cardHolderName', e.target.value)} />
                  </div>
                  {errors.cardHolderName && <p className="mt-1.5 text-sm text-red-500">{errors.cardHolderName}</p>}
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Applicant User ID *</label>
                  <div className="relative">
                    <FileText className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input className={inputClass('userId')} placeholder="e.g. 1024"
                      value={form.userId} onChange={(e) => set('userId', e.target.value.replace(/\D/g, ''))} />
                  </div>
                  {errors.userId && <p className="mt-1.5 text-sm text-red-500">{errors.userId}</p>}
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Mobile Number *</label>
                  <div className="relative">
                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input className={inputClass('mobileNumber')} placeholder="10-digit number" maxLength={10}
                      value={form.mobileNumber} onChange={(e) => set('mobileNumber', e.target.value.replace(/\D/g, ''))} />
                  </div>
                  {errors.mobileNumber && <p className="mt-1.5 text-sm text-red-500">{errors.mobileNumber}</p>}
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Card Network</label>
                  <div className="relative">
                    <CreditCard className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <select className={inputClass('vendor')} value={form.vendor} onChange={(e) => set('vendor', e.target.value)}>
                      <option value="VISA">VISA</option>
                      <option value="MASTERCARD">MASTERCARD</option>
                      <option value="RUPAY">RUPAY</option>
                    </select>
                  </div>
                </div>
              </div>

              {/* Card category selection */}
              <div className="pt-2">
                <label className="block text-sm font-semibold text-gray-700 mb-3">Card Category *</label>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {cardOptions.map((card) => {
                    const isSel = form.cardType === card.type;
                    return (
                      <div key={card.type} onClick={() => set('cardType', card.type)}
                        className={`cursor-pointer rounded-2xl p-5 border transition-all relative ${
                          isSel ? 'shadow-lg ring-2 ring-primary-400 border-primary-200 bg-primary-50/40' : 'border-gray-200 bg-white hover:shadow-md hover:-translate-y-0.5'
                        }`}>
                        {isSel && <CheckCircle className="absolute top-3 right-3 w-5 h-5 text-green-500" />}
                        <CreditCard className={`w-8 h-8 mb-3 ${isSel ? 'text-primary-600' : 'text-gray-400'}`} />
                        <h3 className="text-lg font-bold text-gray-900 mb-2">{card.type}</h3>
                        <ul className="space-y-1">
                          {card.features.map((f, i) => (
                            <li key={i} className="text-sm text-gray-600 flex items-center gap-2">
                              <ShieldCheck className="w-4 h-4 text-gray-400" />{f}
                            </li>
                          ))}
                        </ul>
                      </div>
                    );
                  })}
                </div>
                {errors.cardType && <p className="mt-2 text-sm text-red-500">{errors.cardType}</p>}
              </div>
            </div>

            {/* Section 2 — Consent & Verification */}
            <div className="space-y-6">
              <SectionHeader icon={FileSignature} title="2. Applicant Consent & Verification" />
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-3">Consent Source * <span className="font-normal text-gray-400">(select all that apply)</span></label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {CONSENT_OPTIONS.map((opt) => {
                    const checked = form.consentSources.includes(opt.value);
                    return (
                      <label key={opt.value} className={`flex items-center gap-3 px-4 py-3 rounded-xl border cursor-pointer transition-all ${
                        checked ? 'border-primary-400 bg-primary-50/50' : 'border-gray-200 hover:border-gray-300'
                      }`}>
                        <input type="checkbox" checked={checked} onChange={() => toggleConsent(opt.value)}
                          className="w-4 h-4 text-primary-600 rounded focus:ring-primary-500" />
                        <span className="text-sm font-medium text-gray-700">{opt.label}</span>
                      </label>
                    );
                  })}
                </div>
                {errors.consentSources && <p className="mt-2 text-sm text-red-500">{errors.consentSources}</p>}
              </div>

              {/* OTP verification */}
              <div className="bg-gray-50 rounded-xl border border-gray-200 p-5">
                <div className="flex items-center gap-2 mb-3">
                  <KeyRound className="w-4 h-4 text-primary-600" />
                  <h3 className="text-sm font-semibold text-gray-800">OTP Verification</h3>
                  {otpVerified && (
                    <span className="ml-auto inline-flex items-center gap-1 text-xs font-semibold text-green-700 bg-green-100 px-2.5 py-1 rounded-full">
                      <CheckCircle className="w-3.5 h-3.5" /> Verified
                    </span>
                  )}
                </div>
                <p className="text-xs text-gray-500 mb-3">Send a one-time code to the applicant's phone to authorise this application.</p>
                <div className="flex flex-wrap items-center gap-3">
                  <button type="button" onClick={sendOtp} disabled={otpVerified}
                    className="px-4 py-2 rounded-lg text-sm font-medium bg-white border border-gray-300 hover:bg-gray-50 disabled:opacity-50">
                    {generatedOtp ? 'Resend OTP' : 'Send OTP'}
                  </button>
                  <input value={otpInput} onChange={(e) => setOtpInput(e.target.value.replace(/\D/g, ''))} maxLength={6}
                    placeholder="Enter 6-digit OTP" disabled={!generatedOtp || otpVerified}
                    className="px-3 py-2 rounded-lg border border-gray-300 text-sm w-40 tracking-widest disabled:bg-gray-100" />
                  <button type="button" onClick={verifyOtp} disabled={!generatedOtp || otpVerified}
                    className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 disabled:opacity-50">
                    Verify
                  </button>
                </div>
                {errors.otp && <p className="mt-2 text-sm text-red-500">{errors.otp}</p>}
              </div>
            </div>

            {/* Section 3 — Application Metadata */}
            <div className="space-y-6">
              <SectionHeader icon={Building2} title="3. Application Metadata" />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Lead Source / Channel *</label>
                  <div className="relative">
                    <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <select className={inputClass('leadSource')} value={form.leadSource} onChange={(e) => set('leadSource', e.target.value)}>
                      <option value="">Select channel</option>
                      {LEAD_SOURCES.map((l) => <option key={l.value} value={l.value}>{l.label}</option>)}
                    </select>
                  </div>
                  {errors.leadSource && <p className="mt-1.5 text-sm text-red-500">{errors.leadSource}</p>}
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-2">Sourcing Branch Code *</label>
                  <div className="relative">
                    <FileText className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
                    <input className={inputClass('sourcingBranchCode')} placeholder="e.g. BR-DEL-014"
                      value={form.sourcingBranchCode} onChange={(e) => set('sourcingBranchCode', e.target.value.toUpperCase())} />
                  </div>
                  {errors.sourcingBranchCode && <p className="mt-1.5 text-sm text-red-500">{errors.sourcingBranchCode}</p>}
                </div>
              </div>
            </div>

            {/* Section 4 — Document Uploads */}
            <div className="space-y-6">
              <SectionHeader icon={Upload} title="4. Document Uploads" />
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <FileField label="KYC Document Scan *" field="kycDocumentName" form={form} set={set} error={errors.kycDocumentName} />
                <FileField label="Income Document Scan *" field="incomeDocumentName" form={form} set={set} error={errors.incomeDocumentName} />
              </div>

              {/* Live photograph */}
              <div className="bg-gray-50 rounded-xl border border-gray-200 p-5">
                <div className="flex items-center gap-2 mb-3">
                  <Camera className="w-4 h-4 text-primary-600" />
                  <h3 className="text-sm font-semibold text-gray-800">Live Photograph / Video-KYC *</h3>
                  {photoCaptured && (
                    <span className="ml-auto inline-flex items-center gap-1 text-xs font-semibold text-green-700 bg-green-100 px-2.5 py-1 rounded-full">
                      <CheckCircle className="w-3.5 h-3.5" /> Captured
                    </span>
                  )}
                </div>

                {!cameraOn && !photoCaptured && (
                  <button type="button" onClick={startCamera}
                    className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 flex items-center gap-2">
                    <Camera className="w-4 h-4" /> Start Camera
                  </button>
                )}

                {cameraOn && (
                  <div className="space-y-3">
                    <video ref={videoRef} autoPlay playsInline className="rounded-lg w-64 border border-gray-300" />
                    <div className="flex gap-3">
                      <button type="button" onClick={capturePhoto} className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-green-600 hover:bg-green-700 flex items-center gap-2">
                        <Camera className="w-4 h-4" /> Capture
                      </button>
                      <button type="button" onClick={stopCamera} className="px-4 py-2 rounded-lg text-sm font-medium border border-gray-300 hover:bg-gray-100">Cancel</button>
                    </div>
                  </div>
                )}

                {photoCaptured && photoUrl && (
                  <div className="flex items-center gap-4">
                    <img src={photoUrl} alt="Applicant" className="w-28 h-28 object-cover rounded-lg border border-gray-300" />
                    <button type="button" onClick={() => { setPhotoCaptured(false); setPhotoUrl(''); startCamera(); }}
                      className="px-3 py-2 rounded-lg text-sm font-medium border border-gray-300 hover:bg-gray-100 flex items-center gap-2">
                      <RefreshCw className="w-4 h-4" /> Retake
                    </button>
                  </div>
                )}
                <canvas ref={canvasRef} className="hidden" />
                {errors.photo && <p className="mt-2 text-sm text-red-500">{errors.photo}</p>}
              </div>
            </div>

            {/* Submit */}
            <div className="pt-8 border-t border-gray-100 flex justify-center">
              <button type="submit" disabled={loading}
                className="w-full sm:w-auto min-w-[260px] flex justify-center items-center py-4 px-8 rounded-xl shadow-lg text-base font-bold text-white bg-gradient-to-r from-primary-600 to-primary-700 hover:from-primary-700 hover:to-primary-800 disabled:opacity-50 transition-all active:scale-95">
                {loading ? (
                  <span className="flex items-center gap-3">
                    <span className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Processing Application...
                  </span>
                ) : 'Submit Application'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

// Simple file picker that records the selected file name (demo: no upload backend).
const FileField = ({ label, field, form, set, error }) => (
  <div>
    <label className="block text-sm font-semibold text-gray-700 mb-2">{label}</label>
    <label className={`flex items-center gap-3 px-4 py-3 rounded-xl border cursor-pointer transition-all ${
      error ? 'border-red-300 bg-red-50' : form[field] ? 'border-green-300 bg-green-50' : 'border-gray-200 bg-gray-50 hover:border-gray-300'
    }`}>
      <Upload className={`w-5 h-5 ${form[field] ? 'text-green-600' : 'text-gray-400'}`} />
      <span className="text-sm text-gray-600 truncate">
        {form[field] || 'Choose high-resolution scan (PDF/JPG/PNG)'}
      </span>
      <input type="file" accept="image/*,application/pdf" className="hidden"
        onChange={(e) => set(field, e.target.files?.[0]?.name || '')} />
    </label>
    {error && <p className="mt-1.5 text-sm text-red-500">{error}</p>}
  </div>
);

export default ApplyCreditCard;
