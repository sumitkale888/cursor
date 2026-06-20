import React, {useCallback, useEffect, useState} from 'react';
import {
  Alert,
  Image,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import {NativeStackScreenProps} from '@react-navigation/native-stack';
import {parseApiError} from '../api/client';
import * as productApi from '../api/productApi';
import {Button} from '../components/Button';
import {ErrorView} from '../components/ErrorView';
import {Header} from '../components/Header';
import {LoadingSpinner} from '../components/LoadingSpinner';
import {useCart} from '../context/CartContext';
import {HomeStackParamList} from '../navigation/types';
import {Product} from '../types';
import {colors} from '../theme/colors';
import {radius, spacing} from '../theme/spacing';
import {formatCurrency} from '../utils/format';

type Props = NativeStackScreenProps<
  HomeStackParamList | {ProductDetail: {productId: number}},
  'ProductDetail'
>;

export function ProductDetailScreen({navigation, route}: Props) {
  const {productId} = route.params;
  const {addItem} = useCart();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState<string | null>(null);

  const loadProduct = useCallback(async () => {
    setError(null);
    try {
      const data = await productApi.getProductById(productId);
      setProduct(data);
    } catch (err) {
      setError(parseApiError(err).message);
    } finally {
      setLoading(false);
    }
  }, [productId]);

  useEffect(() => {
    loadProduct();
  }, [loadProduct]);

  const handleAddToCart = async () => {
    if (!product) {
      return;
    }
    setAdding(true);
    try {
      await addItem(product.id, quantity);
      Alert.alert('Added to Cart', `${product.name} x${quantity}`);
    } catch (err) {
      Alert.alert('Error', (err as Error).message);
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return <LoadingSpinner fullScreen />;
  }

  if (error || !product) {
    return (
      <ErrorView
        message={error ?? 'Product not found'}
        onRetry={loadProduct}
      />
    );
  }

  return (
    <View style={styles.container}>
      <Header
        title="Product Details"
        showBack
        onBack={() => navigation.goBack()}
      />
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.imageWrap}>
          {product.imageUrl ? (
            <Image source={{uri: product.imageUrl}} style={styles.image} />
          ) : (
            <View style={styles.placeholder}>
              <Text style={styles.placeholderIcon}>🛒</Text>
            </View>
          )}
        </View>

        <View style={styles.body}>
          <Text style={styles.category}>{product.categoryName}</Text>
          <Text style={styles.name}>{product.name}</Text>
          <Text style={styles.price}>{formatCurrency(product.price)}</Text>
          <Text style={styles.stock}>
            {product.stock > 0
              ? `${product.stock} in stock`
              : 'Out of stock'}
          </Text>
          {product.description ? (
            <Text style={styles.description}>{product.description}</Text>
          ) : null}

          <View style={styles.quantityRow}>
            <Text style={styles.quantityLabel}>Quantity</Text>
            <View style={styles.quantityControls}>
              <Text
                style={styles.qtyBtn}
                onPress={() => setQuantity(q => Math.max(1, q - 1))}>
                −
              </Text>
              <Text style={styles.qtyValue}>{quantity}</Text>
              <Text
                style={styles.qtyBtn}
                onPress={() =>
                  setQuantity(q => Math.min(product.stock, q + 1))
                }>
                +
              </Text>
            </View>
          </View>

          <Button
            title="Add to Cart"
            onPress={handleAddToCart}
            loading={adding}
            disabled={product.stock <= 0}
            style={styles.addBtn}
          />
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingBottom: spacing.xxxl,
  },
  imageWrap: {
    height: 260,
    backgroundColor: colors.primaryLight,
  },
  image: {
    width: '100%',
    height: '100%',
    resizeMode: 'cover',
  },
  placeholder: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  placeholderIcon: {
    fontSize: 64,
  },
  body: {
    padding: spacing.xl,
    backgroundColor: colors.surface,
    borderTopLeftRadius: radius.xl,
    borderTopRightRadius: radius.xl,
    marginTop: -spacing.xl,
  },
  category: {
    fontSize: 13,
    color: colors.primary,
    fontWeight: '600',
    marginBottom: spacing.xs,
  },
  name: {
    fontSize: 24,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.sm,
  },
  price: {
    fontSize: 22,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.xs,
  },
  stock: {
    fontSize: 13,
    color: colors.textSecondary,
    marginBottom: spacing.lg,
  },
  description: {
    fontSize: 15,
    color: colors.textSecondary,
    lineHeight: 22,
    marginBottom: spacing.xl,
  },
  quantityRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: spacing.xl,
  },
  quantityLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: colors.text,
  },
  quantityControls: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.primaryLight,
    borderRadius: radius.md,
    padding: spacing.sm,
  },
  qtyBtn: {
    fontSize: 22,
    fontWeight: '700',
    color: colors.primary,
    paddingHorizontal: spacing.lg,
  },
  qtyValue: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.text,
    minWidth: 32,
    textAlign: 'center',
  },
  addBtn: {
    marginTop: spacing.sm,
  },
});
