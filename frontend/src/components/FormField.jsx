import TextField from "@mui/material/TextField";
export default function FormField({
  name,
  label,
  register,
  errors,
  rules,
  ...props
}) {
  return (
    <TextField
      fullWidth
      margin="normal"
      label={label}
      error={Boolean(errors[name])}
      helperText={errors[name]?.message}
      {...register(name, rules)}
      {...props}
    />
  );
}
