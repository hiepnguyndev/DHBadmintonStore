import TextField from "@mui/material/TextField";
import IconButton from "@mui/material/IconButton";
import InputAdornment from "@mui/material/InputAdornment";
import { useState } from "react";
import { Eye, EyeOff } from "./icons";
export default function FormField({
  name,
  label,
  register,
  errors,
  rules,
  ...props
}) {
  const [visible, setVisible] = useState(false);
  const password = props.type === "password";
  return (
    <TextField
      fullWidth
      margin="normal"
      label={label}
      error={Boolean(errors[name])}
      helperText={errors[name]?.message}
      {...register(name, rules)}
      {...props}
      type={password && visible ? "text" : props.type}
      InputProps={password ? { endAdornment: <InputAdornment position="end"><IconButton size="small" type="button" aria-label={visible ? "Ẩn mật khẩu" : "Hiện mật khẩu"} title={visible ? "Ẩn mật khẩu" : "Hiện mật khẩu"} onClick={() => setVisible(!visible)}>{visible ? <EyeOff size={17} /> : <Eye size={17} />}</IconButton></InputAdornment> } : props.InputProps}
    />
  );
}
