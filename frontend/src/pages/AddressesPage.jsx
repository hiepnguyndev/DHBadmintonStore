import { useEffect, useState } from "react";
import Alert from "@mui/material/Alert";
import Button from "@mui/material/Button";
import Checkbox from "@mui/material/Checkbox";
import Chip from "@mui/material/Chip";
import CircularProgress from "@mui/material/CircularProgress";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import FormControlLabel from "@mui/material/FormControlLabel";
import { useForm } from "react-hook-form";
import { userApi } from "../services/userApi";
import { errorMessage, applyServerErrors } from "../services/apiClient";
import FormField from "../components/FormField";
import { MapPin, Pencil, Plus, Trash2 } from "../components/icons";
const empty = {
  receiverName: "",
  receiverPhone: "",
  province: "",
  ward: "",
  detailAddress: "",
  isDefault: false,
};
const fields = [
  ["receiverName", "Tên người nhận", 100],
  ["receiverPhone", "Số điện thoại người nhận", 20],
  ["province", "Tỉnh / Thành phố", 100],
  ["ward", "Xã / Phường", 100],
  ["detailAddress", "Địa chỉ chi tiết", 255],
];
export default function AddressesPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setErrorMessage] = useState("");
  const [notice, setNotice] = useState("");
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [deleting, setDeleting] = useState(null);
  const [busy, setBusy] = useState(false);
  const [formError, setFormError] = useState("");
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors, isSubmitting },
  } = useForm({ defaultValues: empty });
  async function load() {
    setLoading(true);
    setErrorMessage("");
    try {
      setItems(await userApi.addresses());
    } catch (e) {
      setErrorMessage(errorMessage(e));
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    load();
  }, []);
  function edit(item) {
    setEditing(item?.addressId || null);
    reset(
      item
        ? Object.fromEntries(Object.keys(empty).map((k) => [k, item[k]]))
        : empty,
    );
    setFormError("");
    setOpen(true);
  }
  async function submit(data) {
    setFormError("");
    try {
      if (editing) await userApi.updateAddress(editing, data);
      else await userApi.createAddress(data);
      setOpen(false);
      setNotice("Đã lưu địa chỉ.");
      await load();
    } catch (e) {
      setFormError(errorMessage(e));
      applyServerErrors(e, setError);
    }
  }
  async function remove() {
    setBusy(true);
    try {
      await userApi.deleteAddress(deleting.addressId);
      setDeleting(null);
      setNotice("Đã xóa địa chỉ.");
      await load();
    } catch (e) {
      setErrorMessage(errorMessage(e));
      setDeleting(null);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="page-container">
      <div className="page-heading">
        <div>
          <span className="eyebrow">SỔ ĐỊA CHỈ</span>
          <h1>Giao đến nơi bạn muốn.</h1>
        </div>
        <Button variant="contained" startIcon={<Plus size={17} />} onClick={() => edit(null)}>
          Thêm địa chỉ
        </Button>
      </div>
      {notice && (
        <Alert severity="success" onClose={() => setNotice("")}>
          {notice}
        </Alert>
      )}
      {error && (
        <Alert
          severity="error"
          action={<Button onClick={load}>Thử lại</Button>}
        >
          {error}
        </Alert>
      )}
      {loading ? (
        <CircularProgress aria-label="Đang tải địa chỉ" />
      ) : !error && items.length === 0 ? (
        <div className="empty-state">
          <span className="empty-symbol" aria-hidden="true">
            <MapPin size={28} />
          </span>
          <h2>Chưa có địa chỉ nào</h2>
          <p>Thêm địa chỉ nhận hàng đầu tiên của bạn.</p>
          <Button variant="outlined" onClick={() => edit(null)}>
            Thêm địa chỉ đầu tiên
          </Button>
        </div>
      ) : (
        <div className="address-grid">
          {items.map((item) => (
            <article className="panel" key={item.addressId}>
              <div className="address-title">
                <h2>{item.receiverName}</h2>
                {item.isDefault && (
                  <Chip size="small" color="success" label="Mặc định" />
                )}
              </div>
              <p>{item.receiverPhone}</p>
              <p className="muted">
                {item.detailAddress}
                <br />
                {item.ward}, {item.province}
              </p>
              <Button startIcon={<Pencil size={15} />} onClick={() => edit(item)}>Chỉnh sửa</Button>
              <Button startIcon={<Trash2 size={15} />} color="error" onClick={() => setDeleting(item)}>
                Xóa
              </Button>
            </article>
          ))}
        </div>
      )}
      <Dialog
        open={open}
        onClose={() => !isSubmitting && setOpen(false)}
        fullWidth
        maxWidth="sm"
      >
        <form onSubmit={handleSubmit(submit)}>
          <DialogTitle>
            {editing ? "Chỉnh sửa địa chỉ" : "Thêm địa chỉ"}
          </DialogTitle>
          <DialogContent>
            {formError && <Alert severity="error">{formError}</Alert>}
            {fields.map(([name, label, max]) => (
              <FormField
                key={name}
                name={name}
                label={label}
                register={register}
                errors={errors}
                rules={{
                  required: "Vui lòng nhập thông tin",
                  maxLength: { value: max, message: `Tối đa ${max} ký tự` },
                }}
              />
            ))}
            <FormControlLabel
              control={<Checkbox {...register("isDefault")} />}
              label="Đặt làm địa chỉ mặc định"
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpen(false)} disabled={isSubmitting}>
              Hủy
            </Button>
            <Button type="submit" variant="contained" disabled={isSubmitting}>
              {isSubmitting ? "Đang lưu…" : "Lưu địa chỉ"}
            </Button>
          </DialogActions>
        </form>
      </Dialog>
      <Dialog
        open={Boolean(deleting)}
        onClose={() => !busy && setDeleting(null)}
      >
        <DialogTitle>Xóa địa chỉ?</DialogTitle>
        <DialogContent>
          Bạn muốn xóa địa chỉ của {deleting?.receiverName}?
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleting(null)} disabled={busy}>
            Giữ lại
          </Button>
          <Button color="error" onClick={remove} disabled={busy}>
            Xóa địa chỉ
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
